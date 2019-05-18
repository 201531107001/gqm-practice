package com.nio.chat;


import org.junit.Test;

import java.nio.ByteBuffer;

public class ByteBufferTest {

	@Test
	public void writeAndRead(){
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		byteBuffer.put("hello".getBytes());

		// 切换到读模式
		byteBuffer.flip();
		while (byteBuffer.position()<byteBuffer.limit()){
			System.out.print(byteBuffer.get()+" ");
		}

		System.out.println();

		// 下次写之前清除缓存
		byteBuffer.clear();
		byteBuffer.put("hello".getBytes());
		byteBuffer.flip();
		while (byteBuffer.position()<byteBuffer.limit()){
			System.out.print(byteBuffer.get()+" ");
		}
	}

	@Test
	public void writeAndReadAndReset(){
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		byteBuffer.put("hello".getBytes());

		// 标记当前的pos位置
		byteBuffer.mark();

		// 切换到读模式,会将mark设置为-1，所以下面的reset是错误的操作
		byteBuffer.flip();
		while (byteBuffer.position()<byteBuffer.limit()){
			System.out.print(byteBuffer.get()+" ");
		}

		System.out.println();
		System.out.println(byteBuffer.position()+" "+byteBuffer.capacity()+" "+byteBuffer.limit());

		// 重置缓存
		byteBuffer.reset();

		byteBuffer.put("hello".getBytes());
		byteBuffer.flip();
		while (byteBuffer.position()<byteBuffer.limit()){
			System.out.print(byteBuffer.get()+" ");
		}
	}
}