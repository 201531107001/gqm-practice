package com.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class NioServer {

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
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

		System.out.println("服务端启动");

		int readyChannels = 0;
		while (true) {
			try {
				// 获取准备好操作的通道数量
				// int readyChannels = selector.selectNow();
				readyChannels = selector.select(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (readyChannels < 1) {
				continue;
			}

			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				try {
					// 处理客户端的连接
					if (selectionKey.isAcceptable()) {
						acceptHandler(serverSocketChannel, selector);
					}

					// 读取客户端的数据
					if (selectionKey.isReadable()) {
						readHandler(selectionKey, selector);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 将客户端的消息广播到所有用户
	 *
	 * @param selector
	 * @param socketChannel
	 * @param content
	 */
	public void toAllUser(Selector selector, SocketChannel socketChannel, String content) {
		Set<SelectionKey> selectionKeySet = selector.keys();
		ByteBuffer byteBuffer = Charset.forName("utf-8").encode(content);
		selectionKeySet.parallelStream().forEach(key -> {
			if (key.channel() instanceof SocketChannel) {
				SocketChannel clientChannel = (SocketChannel) key.channel();
				if (!Objects.equals(clientChannel, socketChannel)) {
					byteBuffer.position(0);
					try {
						clientChannel.write(byteBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 处理客户端的连接
	 *
	 * @param serverSocketChannel
	 * @param selector
	 * @throws IOException
	 */
	public void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
		// 获取客户端的通道
		SocketChannel socketChannel = serverSocketChannel.accept();

		System.out.println("有客户端连接,端口为:" + socketChannel.socket().getPort());

		toAllUser(selector, socketChannel, "系统消息:" + socketChannel.socket().getPort() + "上线");

		// 设置通道为非阻塞
		socketChannel.configureBlocking(false);

		// 客户端通道注册到选择器
		socketChannel.register(selector, SelectionKey.OP_READ);

//		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//		byteBuffer.clear();
//		byteBuffer.put("hello my client".getBytes());
//		byteBuffer.flip(); // 切换为读模式
		socketChannel.write(Charset.forName("utf-8").encode("hello my client"));
	}

	/**
	 * 读取客户端的数据
	 *
	 * @param selectionKey
	 * @param selector
	 * @throws IOException
	 */
	public void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
		// 获取客户端通道
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		byteBuffer.clear();

		String request = "";
		int k;
		// 读取客户端数据
		while (true) {
			k = socketChannel.read(byteBuffer);
			// 客户端断开连接
			if (k == -1) {
				System.out.println(":" + socketChannel.socket().getPort() + "断开连接");
				toAllUser(selector, socketChannel, "系统消息:" + socketChannel.socket().getPort() + "下线");
				socketChannel.close();
				return;
			}

			if (k == 0) {
				break;
			}

			// 切换为读模式
			byteBuffer.flip();
			request = request + Charset.forName("utf-8").decode(byteBuffer);

			// buffer用完后清空缓存，以备下次使用
			byteBuffer.clear();
		}

		// 将通道再次注册到选择器上
		socketChannel.register(selector, SelectionKey.OP_READ);

		// 输出客户端数据到所有客户端
		if (request.length() > 0) {
			System.out.println("::" + request);
			toAllUser(selector, socketChannel, socketChannel.socket().getPort() + ":" + request);
		}
	}
}
