package com.auyeung.provider.api;

import com.auyeung.api.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        return String.format("hello %s", name);
    }
}
