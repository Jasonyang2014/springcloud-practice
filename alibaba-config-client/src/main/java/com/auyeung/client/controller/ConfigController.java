package com.auyeung.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    @Value("${useLocalCache}")
    private boolean useLocalCache;

    @Value("${env}")
    private String env;

    @RequestMapping("/get")
    public String get() {
        return String.format("%s %b", env, useLocalCache);
    }
}
