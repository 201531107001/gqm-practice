package com.me.java.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface IVehical {
    void run(int speed);
}

//concrete implementation
class Car implements IVehical {
    public void run(int speed) {
        System.out.println("Car is running,speed:"+speed);
    }

}

//proxy class
class VehicalProxy {

    private IVehical vehical;

    public VehicalProxy(IVehical vehical) {
        this.vehical = vehical;
    }

    public IVehical create() {
        final Class<?>[] interfaces = new Class[] { IVehical.class };
        final VehicalInvacationHandler handler = 
                new VehicalInvacationHandler(vehical);

        return (IVehical) Proxy.newProxyInstance
                (IVehical.class.getClassLoader(), interfaces, handler);
    }

    public class VehicalInvacationHandler implements InvocationHandler {

        private final IVehical vehical;

        public VehicalInvacationHandler(IVehical vehical) {
            this.vehical = vehical;
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            System.out.println("--before running...");
            Object ret = method.invoke(vehical, args);
            System.out.println("--after running...");
            return ret;
        }
    }
}

public class ProxyToAOP {
    public static void main(String[] args) {

        IVehical car = new Car();
        VehicalProxy proxy = new VehicalProxy(car);

        // 这个对象将会代理car
        IVehical proxyObj = proxy.create();
        proxyObj.run(50);
        car.run(30);
    }
}
/*
 * output: --before running... Car is running --after running...
 */
