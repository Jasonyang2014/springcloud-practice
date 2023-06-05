package com.auyeung.sentinel.fallback;

import com.auyeung.sentinel.api.OrderApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderApiFallback implements OrderApi {
    @Override
    public int createOrder(Integer userId) {
        //默认失败0
        log.error("call order api error");
        return 0;
    }
}
