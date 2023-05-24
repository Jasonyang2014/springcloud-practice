package com.auyeung.config.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class TestController {

    @Value("${application.name}")
    private String appName;

    @GetMapping("/test")
    public String test(){
        return appName;
    }
}
