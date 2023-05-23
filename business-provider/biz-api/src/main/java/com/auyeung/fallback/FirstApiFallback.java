package com.auyeung.fallback;

import com.auyeung.api.FirstApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirstApiFallback implements FirstApi {
    @Override
    public String echo(String param) {
        log.info("first api echo fallback {} error.", param);
        return param;
    }
}
