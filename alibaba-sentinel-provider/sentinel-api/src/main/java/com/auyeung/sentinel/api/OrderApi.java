package com.auyeung.sentinel.api;

import com.auyeung.sentinel.fallback.OrderApiFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "alibaba-sentinel-service", contextId = "order-api",
        path = "/order",
        fallback = OrderApiFallback.class)
public interface OrderApi {

    @RequestMapping("/create")
    int createOrder(@RequestParam Integer userId);
}
