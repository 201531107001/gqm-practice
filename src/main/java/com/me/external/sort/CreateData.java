package com.me.external.sort;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 创造数据
 * @author 清明
 *
 */
public class CreateData {
	public static void main(String[] args) throws IOException {
		DataOutputStream outputStream = 
		        FileUtils.getOutputStream("data/largeData.dat");
		
		Random random = new Random();
		// 这里将数据写入到文件里
		for(int i=0;i<100;i++)
			outputStream.writeInt(random.nextInt()%100);
		
		outputStream.close();
		
		DataInputStream inputStream = 
		        FileUtils.getInputStream("data/largeData.dat");
		
		for(int i=0;i<100;i++) {
			if((i+1)%20==0)
				System.out.println();
			System.out.print(inputStream.readInt()+" ");
		}
		inputStream.close();
	}
}
