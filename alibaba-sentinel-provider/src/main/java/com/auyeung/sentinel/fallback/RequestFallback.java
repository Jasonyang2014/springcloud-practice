package com.auyeung.sentinel.fallback;

public class RequestFallback {

    public static String fallback(Throwable throwable) {
        return "request error fallback " + throwable;
    }
}
