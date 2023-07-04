package com.auyeung.stream.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class MQConsumer {

    @Bean
    public Consumer<Message<String>> consumeMsg() {
        return msg -> {
            log.info(Thread.currentThread().getName() + " Consumer Receive New Messages: " + msg);
        };
    }
}
