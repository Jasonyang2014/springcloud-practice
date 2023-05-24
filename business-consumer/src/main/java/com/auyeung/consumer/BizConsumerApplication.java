package com.auyeung.consumer;

import feign.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableHystrix
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.auyeung.api"})
@SpringBootApplication(scanBasePackages = {"com.auyeung.fallback", "com.auyeung.consumer"})
public class BizConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizConsumerApplication.class);
    }


    @Bean
    Logger.Level logger(){
        return Logger.Level.FULL;
    }
}
