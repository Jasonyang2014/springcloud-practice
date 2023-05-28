package com.auyeung.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello(){
        log.info("start controller hello");
        return "hello world!";
    }
}
//[{"traceId":"369f129c673be307","id":"369f129c673be307","kind":"SERVER","name":"get /hello","timestamp":1685256218230595,"duration":28349,"localEndpoint":{"serviceName":"biz-service","ipv4":"192.168.203.167"},"remoteEndpoint":{"ipv6":"::1","port":50899},"tags":{"http.method":"GET","http.path":"/hello","mvc.controller.class":"TestController","mvc.controller.method":"hello"}}]
//curl -X POST -d '[{"traceId":"369f129c673be307","id":"369f129c673be307","kind":"SERVER","name":"get /hello","timestamp":1685256218230595,"duration":28349,"localEndpoint":{"serviceName":"biz-service","ipv4":"192.168.203.167"},"remoteEndpoint":{"ipv6":"::1","port":50899},"tags":{"http.method":"GET","http.path":"/hello","mvc.controller.class":"TestController","mvc.controller.method":"hello"}}]' \
//-H 'content-type:application/json' http://192.18.134.94:8848/api/v2/spans