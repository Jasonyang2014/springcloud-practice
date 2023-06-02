package com.auyeung.api;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {

    @Test
    public void test() {
        Api api = (Api) Proxy.newProxyInstance(ProxyTest.class.getClassLoader(),
                new Class[]{Api.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    int length = args == null ? 0 : args.length;
                    Anno annotation = method.getAnnotation(Anno.class);
                    String format = String.format("method %s args %d annotation %s", name, length, annotation.annotationType().getName());
                    System.out.println(format);
                    return format;
                }
        );

        try {
            Method method = api.getClass().getMethod("hello");
            Anno annotation = method.getAnnotation(Anno.class);
            System.out.println(annotation);

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        String hello = api.hello();
        System.out.println(hello);
    }


}

interface Api {

    @Anno
    String hello();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@interface Anno {

}
