package com.auyeung.sentinel.controller;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.auyeung.sentinel.fallback.RequestFallback;
import com.auyeung.sentinel.handler.RequestBlockHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "hello world!";
    }


    @GetMapping("/test")
    @SentinelResource(value = "test",
            entryType = EntryType.IN,
            blockHandlerClass = RequestBlockHandler.class, blockHandler = "tooQuick"
    )
    public String test() {
        return "test";
    }


    @GetMapping("/err")
    @SentinelResource(value = "err",
            entryType = EntryType.IN,
            fallbackClass = RequestFallback.class, fallback = "fallback"
    )
    public String error() {
        int i = ThreadLocalRandom.current().nextInt();
        if (i % 2 != 0) {
            throw new RuntimeException("error");
        }
        return "error";
    }
}
