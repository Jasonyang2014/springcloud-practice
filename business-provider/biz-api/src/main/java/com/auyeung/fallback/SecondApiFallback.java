package com.auyeung.fallback;

import com.auyeung.api.SecondApi;
import org.springframework.stereotype.Component;

@Component
public class SecondApiFallback implements SecondApi {
    @Override
    public String sleep(String times) {
        return "sleep fallback";
    }
}
