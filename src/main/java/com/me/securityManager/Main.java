package com.me.securityManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        SecurityManager sm = System.getSecurityManager();
        
        if(sm != null) {
            System.out.println("hello SecurityManager");
            sm.checkWrite("data/security.txt");
            sm.checkRead("data/security.txt");
        }
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("data/security.txt"))){
            writer.write("hello SecurityManager");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
