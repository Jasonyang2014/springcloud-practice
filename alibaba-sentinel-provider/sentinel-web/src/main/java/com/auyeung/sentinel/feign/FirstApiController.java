package com.auyeung.sentinel.feign;

import com.auyeung.sentinel.api.FirstApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class FirstApiController implements FirstApi {

    @RequestMapping("/hello")
    @Override
    public String hello() {
        return "hello world!";
    }
}
