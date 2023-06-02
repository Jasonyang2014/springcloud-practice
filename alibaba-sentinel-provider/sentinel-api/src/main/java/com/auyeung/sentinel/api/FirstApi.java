package com.auyeung.sentinel.api;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.auyeung.sentinel.fallback.FirstApiFallback;
import com.auyeung.sentinel.fallback.FirstApiMethodFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "alibaba-sentinel-service", contextId = "first-api", fallback = FirstApiFallback.class)
public interface FirstApi {


    @RequestMapping("/hello")
    @SentinelResource(value = "hello",
            fallbackClass = FirstApiMethodFallback.class, fallback = "helloFallback"
    )
    String hello();
}
