package com.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
	private Set clientChannels = new HashSet();

	public static void main(String[] args) throws IOException {
		new NioServer().start();
	}

	public void start() throws IOException {
		// 创建一个选择器
		Selector selector = Selector.open();

		// 先建立连接，然后设置为非阻塞模式，然后注册到选择器
		// 创建服务器通道
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		// 设置端口
		serverSocketChannel.bind(new InetSocketAddress(8888));
		// 设置为非阻塞
		serverSocketChannel.configureBlocking(false);

		// 注册服务通道，监听连接事件
		serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

		System.out.println("服务端启动");

		while (true){
			// 获取准备好操作的通道数量
			// int readyChannels = selector.selectNow();
			int readyChannels = selector.select(500);
			if(readyChannels<1){
				continue;
			}

			Set<SelectionKey> selectionKeys =  selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()){
				SelectionKey selectionKey = iterator.next();
				iterator.remove();

				// 处理客户端的连接
				if(selectionKey.isAcceptable()){
					acceptHandler(serverSocketChannel,selector);
				}

				// 读取客户端的数据
				if(selectionKey.isReadable()){
					readHandler(selectionKey,selector);
				}
			}
		}
	}

	public void acceptHandler(ServerSocketChannel serverSocketChannel,Selector selector) throws IOException {
		// 获取客户端的通道
		SocketChannel socketChannel = serverSocketChannel.accept();
		clientChannels.add(socketChannel);

		System.out.println("有客户端连接,端口为:"+socketChannel.socket().getPort());
		new Thread(new ServerHandler(clientChannels,socketChannel,"系统消息:"+socketChannel.socket().getPort()+"上线")).start();

		// 设置通道为非阻塞
		socketChannel.configureBlocking(false);

		// 客户端通道注册到选择器
		socketChannel.register(selector,SelectionKey.OP_READ);

//		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//		byteBuffer.clear();
//		byteBuffer.put("hello my client".getBytes());
//		byteBuffer.flip(); // 切换为读模式
		socketChannel.write(Charset.forName("utf-8").encode("hello my client"));
	}

	public void readHandler(SelectionKey selectionKey,Selector selector) throws IOException {
		// 获取客户端通道
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		byteBuffer.clear();

		String request = "";
		int k;
		// 读取客户端数据
		while (true){
			k = socketChannel.read(byteBuffer);
			// 客户端断开连接
			if(k == -1){
				clientChannels.remove(socketChannel);
				System.out.println(":"+socketChannel.socket().getPort()+"断开连接");
				new Thread(new ServerHandler(clientChannels,socketChannel,"系统消息:"+socketChannel.socket().getPort()+"下线")).start();
				socketChannel.close();
				return;
			}

			if(k == 0){
				break;
			}

			// 切换为读模式
			byteBuffer.flip();
			request = request + Charset.forName("utf-8").decode(byteBuffer);

			// buffer用完后清空缓存，以备下次使用
			byteBuffer.clear();
		}

		// 将通道再次注册到选择器上
		socketChannel.register(selector,SelectionKey.OP_READ);

		// 输出客户端数据到所有客户端
		if(request.length() > 0) {
			System.out.println("::" + request);
			new Thread(new ServerHandler(clientChannels,socketChannel,socketChannel.socket().getPort()+":"+request)).start();
		}
	}
}
