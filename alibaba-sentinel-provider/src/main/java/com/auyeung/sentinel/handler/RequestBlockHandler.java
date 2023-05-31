package com.auyeung.sentinel.handler;

public class RequestBlockHandler {

    /**
     * block handler
     * 参数必须和原来的请求参数顺序一致，在再加上一个{@link Throwable}
     * 判断条件在 {@link com.alibaba.csp.sentinel.annotation.aspectj.AbstractSentinelAspectSupport#findMethod(boolean, Class, String, Class, Class[])}
     * @param exception
     * @return
     */
    public static String tooQuick(Throwable exception) {
        return String.format("request is too fast! %s", exception.getLocalizedMessage());
    }

}
