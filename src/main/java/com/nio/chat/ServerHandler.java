package com.nio.chat;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * 将客户端的消息广播到所有用户
 */
@AllArgsConstructor
public class ServerHandler implements Runnable {
	private Set<SocketChannel> clientChannels;
	private SocketChannel socketChannel;
	private String content;

	@Override
	public void run() {
		Iterator<SocketChannel> iterator = clientChannels.iterator();
		ByteBuffer byteBuffer = Charset.forName("utf-8").encode(content);
		try {
			while (iterator.hasNext()) {
				SocketChannel clientChannel = iterator.next();
				if (!Objects.equals(clientChannel, socketChannel)) {
					// 设置起始下标为0,这样可以重复利用同一个buffer
					byteBuffer.position(0);
					clientChannel.write(byteBuffer);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
