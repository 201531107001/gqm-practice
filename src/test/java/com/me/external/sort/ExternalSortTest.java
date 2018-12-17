package com.me.external.sort;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.junit.Test;

public class ExternalSortTest {

    @Test
    public void testMergeData() {
        try {
            readData("s1");
            readData("s2");

            ExternalSort.mergeData("s1", "s2");
            readData("s1");
            readData("temp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void read() {
        try {
            readData("s1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeData(String path) throws IOException {
        DataOutputStream temp = FileUtils.getOutputStream("data/temp.dat");
        temp.writeInt(10);
        temp.close();
    }
    
    public static void readData(String path) throws IOException {
        DataInputStream inputStream = FileUtils.getInputStream("data/"+path+".dat");
       int a = 1,i = 0;
        while(true) {
            try {
                i++;
                if(i%10==0)
                    System.out.println();
                a = inputStream.readInt();
                System.out.print(a+" ");
            } catch (EOFException e) {
                break;
            }
        }
        inputStream.close();
    }
}
