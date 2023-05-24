package com.auyeung.api;

import com.auyeung.fallback.SecondApiFallback;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "biz-service",
        contextId = "second-api",
        path = "/second"
//        ,
//        fallback = SecondApiFallback.class)
)
public interface SecondApi {


    @GetMapping("/sleep/{times}")
    String sleep(@PathVariable String times);

}
