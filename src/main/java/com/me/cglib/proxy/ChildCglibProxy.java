package com.me.cglib.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ChildCglibProxy implements MethodInterceptor {
    // 业务类对象，供代理方法中进行真正的业务方法调用
    private Object target;

    // 相当于JDK动态代理中的绑定
    public Object getInstance(Object target) {
        // 给业务对象赋值
        this.target = target;
        // 创建加强器，用来创建动态代理类
        Enhancer enhancer = new Enhancer();
        // 为加强器指定要代理的业务类（即：为下面生成的代理类指定父类）
        enhancer.setSuperclass(this.target.getClass());
        // 设置回调：对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实现
        // intercept()方法进行拦
        enhancer.setCallback(this);
        // 创建动态代理类对象并返回
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println("before run——————");
        // 调用业务类（父类中）的方法
        proxy.invokeSuper(obj, args);
        System.out.println("after run——————");
        return null;
    }

}
