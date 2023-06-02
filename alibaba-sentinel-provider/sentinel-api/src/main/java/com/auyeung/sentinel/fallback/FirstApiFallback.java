package com.auyeung.sentinel.fallback;

import com.auyeung.sentinel.api.FirstApi;
import org.springframework.stereotype.Component;

@Component
public class FirstApiFallback implements FirstApi {
    @Override
    public String hello() {
        return "first api fallback";
    }
}
