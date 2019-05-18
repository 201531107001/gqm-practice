package com.nio.chat;

public class ThreadTest {
	public static void main(String[] args) {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("子线程");
			}
		}).start();
	}
}
