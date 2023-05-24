package com.auyeung.api;

import com.auyeung.fallback.FirstApiFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "biz-service", contextId = "first-api", path = "/first", fallback = FirstApiFallback.class)
public interface FirstApi {


    @GetMapping("/echo/{param}")
    String echo(@PathVariable String param);

}
