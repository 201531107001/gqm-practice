package com.me.external.sort;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/**
 * 外部排序具体实现
 * 结果放到是s1.dat
 * @author 清明
 *
 */
public class ExternalSort {
	public static final int MAX_LENGTH = 10;
	
	public static void main(String[] args) throws IOException {
		int[] list = new int[MAX_LENGTH];
		
		int n = readAndSort(list);
		
		mergeResultFile(n);
	}
	
	/**
	 * 首尾合并数据
	 * @param n 文件个数
	 * @throws IOException
	 */
	public static void mergeResultFile(int n) throws IOException {
	    if(n==1) {
	        return;
	    }
	    
	    if(n%2==1) {
    	    for(int i=1;i<n/2+1;i++) {
    	        mergeData("s"+i, "s"+(n-i+1));
    	    }
    	    mergeResultFile(n/2+1);
	    }else {
	        for(int i=1;i<=n/2;i++) {
                mergeData("s"+i, "s"+(n-i+1));
            }
	        mergeResultFile(n/2);
	    }
	}
	
	// 合并两个文件的数据
	public static void mergeData(String s1,String s2) throws IOException {
	    
	    File file = new File("data/temp.dat");
	    if(file.exists()) {
	        file.delete();
	    }
	    DataInputStream input1 = FileUtils.getInputStream("data/"+s1+".dat");
	    DataInputStream input2 = FileUtils.getInputStream("data/"+s2+".dat");
	    DataOutputStream temp = FileUtils.getOutputStream("data/temp.dat");
		
		int a=0,b=0;
		boolean flag1 = false,flag2 = false;
		while(true) {
			try {
			    if(!flag1) {
    				a = input1.readInt();
    				flag1 = true;
			    }
			} catch (EOFException e) {
				break;
			}
			
			try {
			    if(!flag2) {
    				b = input2.readInt();
    				flag2 = true;
			    }
			} catch (EOFException e) {
				break;
			}
			
			// 将小的放到temp中
			if(a>b) {
				temp.writeInt(b);
				flag2 = false;
			}else {
			    temp.writeInt(a);
                flag1 = false;
			}
		}
		
		//文件s2读完,将s1没有读完的复制到temp
		if(flag1) {
			temp.writeInt(a);
			while(true) {
			    try {
	                a = input1.readInt();
	                temp.writeInt(a);
	            } catch (EOFException e) {
	                break;
	            }
			}
		}
		
		//文件s1读完,将s2没有读完的复制到temp
        if(flag2) {
            temp.writeInt(b);
            while(true) {
                try {
                    b = input2.readInt();
                    temp.writeInt(b);
                } catch (EOFException e) {
                    break;
                }
            }
        }
		
		input1.close();
		input2.close();
		temp.close();
		
		copyTempToS1(s1);
	}
	
	/**
	 * 将临时文件的内容写到第一个文件
	 * @param s1
	 * @throws IOException
	 */
	public static void copyTempToS1(String s1) throws IOException{
	    File file = new File("data/"+s1+".dat");
        if(file.exists()) {
            file.delete();
            file.createNewFile();
        }
        DataOutputStream output = FileUtils.getOutputStream("data/"+s1+".dat");
        DataInputStream temp = FileUtils.getInputStream("data/temp.dat");
        int b = 0;
        while(true) {
            try {
                b = temp.readInt();
                output.writeInt(b);
            } catch (EOFException e) {
                break;
            }
        }
        temp.close();
        output.close();
	}
	
	/**
	 * 读取数据，排序后写入临时文件，临时文件命名从s1开始，
	 * @param list
	 * @return 临时文件的个数
	 * @throws IOException
	 */
	public static int readAndSort(int[] list) throws IOException {
		DataInputStream inputStream = 
		        FileUtils.getInputStream("data/largeData.dat");
		
		int index = 0;
		while(true) {
			int len = readData(list, inputStream);
			index++;
			DataOutputStream outputStream = 
			        FileUtils.getOutputStream("data/s"+index+".dat");
                
            MutilThreadSort.sort(list);
            for(int i=0;i<len;i++) {
                outputStream.writeInt(list[i]);
            }
            outputStream.close();
            if(len < MAX_LENGTH) {
                break;
            }
		}
		return index;
	}
	
	// 读取数据，返回长度
	public static int readData(int[] list,DataInputStream inputStream) throws IOException {
		int i = 0,k=0;
		for(i=0;i<MAX_LENGTH;i++) {
			try {
				k = inputStream.readInt();
			} catch (EOFException e) {
				break;
			}
			list[i] = k;
		}
		return i;
	}
}
