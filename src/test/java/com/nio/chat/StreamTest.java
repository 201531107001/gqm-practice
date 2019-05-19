package com.nio.chat;

import java.util.Arrays;

public class StreamTest {
	public static void main(String[] args) {
		// 判断一下是否并行
		Arrays.asList(1,2,3,4,5,6,7,8,9).parallelStream().forEach(a-> System.out.print(a));
		System.out.println();
		Arrays.asList(1,2,3,4,5,6,7,8,9).stream().forEach(a-> System.out.print(a));
	}
}
