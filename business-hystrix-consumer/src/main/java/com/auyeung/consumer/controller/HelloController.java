package com.auyeung.consumer.controller;

import com.auyeung.api.FirstApi;
import com.auyeung.api.SecondApi;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class HelloController {

    private final FirstApi firstApi;
    private final SecondApi secondApi;


    @GetMapping("/hello/{param}")
    public String hello(@PathVariable String param) {
        log.info("client hello {}", param);
        return firstApi.echo(param);
    }

    @HystrixCommand(fallbackMethod = "timeout", commandProperties = {
            @HystrixProperty(name = HystrixPropertiesManager.EXECUTION_TIMEOUT_ENABLED, value = "true"),
            @HystrixProperty(name = HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = "5000"),
            @HystrixProperty(name = HystrixPropertiesManager.REQUEST_LOG_ENABLED, value = "true")
    })
    @GetMapping("/sleep/{times}")
    public String sleep(@PathVariable String times) {
        return secondApi.sleep(times);
    }

    public String timeout(String s, Throwable e) {
        return String.format("hystrix sleep fallback %s", e.toString());
    }
}
