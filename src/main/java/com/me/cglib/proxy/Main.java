package com.me.cglib.proxy;

public class Main {
    public static void main(String[] args) {
        Child child = new Child();
        ChildCglibProxy childCglibProxy = new ChildCglibProxy();
        child = (Child) childCglibProxy.getInstance(child);
        child.run(50);
    }
}
