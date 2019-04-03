package com.me.classloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

import javax.servlet.Servlet;

public class Main {
    public static void main(String[] args) {
//        String str = "D:\\git project\\test-demo\\webapps\\com/mchage/lang/ByteUtils.class";
//        File file = new File(str);
//        System.out.println(file);
        URL[] urls = new URL[0];
        MyClassLoader classLoader = new MyClassLoader(urls);
        
        //D:\git project\test-demo
        String user_dir = (System.getProperty("user.dir"));
        classLoader.setJarPath(user_dir+"\\mylib");
        FileDirContext fileDirContext = new FileDirContext();
        fileDirContext.setDocBase("D:\\git project\\test-demo\\");
        classLoader.setResources(new ProxyDirContext(System.getenv(),fileDirContext ));
        classLoader.addRepository("webapps\\", new File("webapps"));
        String jarPath = "D:\\git project\\test-demo\\mylib\\c3p0-0.9.1.1.jar";
        try {
            classLoader.addJar(jarPath, new JarFile(jarPath), new File(jarPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            // jar find
            classLoader.findClassInternal("com.mchange.lang.ByteUtils");
            
            // file find
            Class  servletClass = classLoader.findClassInternal("com.tomcat.study.MyServlet");
            Servlet servlet = (Servlet) servletClass.newInstance();
            servlet.destroy();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        
    }
}
