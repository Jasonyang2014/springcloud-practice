package com.auyeung.consumer.controller;

import com.auyeung.api.FirstApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class HelloController {

    @Resource
    private FirstApi firstApi;


    @GetMapping("/hello/{param}")
    public String hello(@PathVariable String param){
        return firstApi.echo(param);
    }
}
