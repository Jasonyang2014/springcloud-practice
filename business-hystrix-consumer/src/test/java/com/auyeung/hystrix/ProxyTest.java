package com.auyeung.hystrix;

import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {

    @Test
    public void testProxy() throws Throwable {
        PreProcessor proxy = (PreProcessor) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{PreProcessor.class}, new PreProcessorMethodHandler());
        MethodType methodType = MethodType.methodType(void.class);
        MethodHandle log = MethodHandles.lookup().bind(proxy, "log", methodType);
        log.invoke();
        proxy.process();
    }
}

class PreProcessorMethodHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        System.out.println("start execute " + name);
        return proxy;
    }
}
