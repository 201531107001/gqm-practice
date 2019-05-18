package com.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NioClient {
	public static void main(String[] args) throws IOException {
		new NioClient().start();
	}

	private ClientHandler clientHandler;

	public void start() throws IOException {
		Selector selector = Selector.open();
		SocketChannel socketChannel = SocketChannel.open();
		// 先建立连接，然后设置为非阻塞模式，然后注册到选择器
		socketChannel.connect(new InetSocketAddress("localhost",8888));
		socketChannel.configureBlocking(false);
		socketChannel.register(selector,SelectionKey.OP_READ);

		if(socketChannel.isConnected()){
			System.out.println("连接到服务器");
		}

		// 启动线程处理服务器的响应
		new Thread(clientHandler = new ClientHandler(selector)).start();

		// 客户端输入
		Scanner input = new Scanner(System.in);
		while (input.hasNext()){
			String str = input.nextLine();
			// 断开连接
			if("quit".equals(str)){
				clientHandler.stop();
				socketChannel.close();
				break;
			}
			socketChannel.write(Charset.forName("utf-8").encode(str));
		}
	}
}
