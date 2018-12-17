package com.me.external.sort;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileUtils {
    
    public static DataInputStream getInputStream(String path) throws FileNotFoundException {
        DataInputStream input = new DataInputStream(
                new BufferedInputStream(
                new FileInputStream(path)));
        return input;
    }
    
    public static DataOutputStream getOutputStream(String path) throws FileNotFoundException {
        DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(path)));
        return output;
    }
}
