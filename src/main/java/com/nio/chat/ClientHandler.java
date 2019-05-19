package com.nio.chat;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 接受服务端响应数据
 */
public class ClientHandler implements Runnable {
	private boolean flag = true;
	private Selector selector;

	public ClientHandler(Selector selector) {
		this.selector = selector;
	}

	public void stop() {
		this.flag = false;
	}

	@Override
	public void run() {
		while (flag) {
			int readyKeys = 0;//selector.selectNow();
			try {
				readyKeys = selector.select(500);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (readyKeys < 1) {
				continue;
			}
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				if (selectionKey.isReadable()) {
					try {
						readHandler(selectionKey, selector);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
		// 获取服务端通道
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		byteBuffer.clear();

		String response = "";
		// 读取服务端端数据
		while (socketChannel.read(byteBuffer) > 0) {
			// 切换为读模式
			byteBuffer.flip();
			response = response + Charset.forName("utf-8").decode(byteBuffer);

			// buffer用完后清空缓存，以备下次使用
			byteBuffer.clear();
		}

		// 将通道再次注册到选择器上
		socketChannel.register(selector, SelectionKey.OP_READ);

		if (response.length() > 0) {
			System.out.println(response);
		}
	}
}
