package com.auyeung.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.cloud.stream.config.MessageSourceCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.amqp.inbound.AmqpMessageSource;

import java.util.function.Function;

//@EnableDiscoveryClient
@Slf4j
@SpringBootApplication
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class);
    }

    @Bean
    Function<String, String> uppercase() {
        return s -> {
            String upperCase = s.toUpperCase();
            log.info("upper case {}", upperCase);
            return upperCase;
        };
    }
}
