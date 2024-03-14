package com.auyeung.consumer.controller;

import com.auyeung.api.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class DemoController {

    @DubboReference
    private DemoService demoService;

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name){
        return demoService.sayHello(name);
    }
}
