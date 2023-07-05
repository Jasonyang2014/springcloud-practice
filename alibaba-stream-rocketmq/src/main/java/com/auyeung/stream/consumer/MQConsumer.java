package com.auyeung.stream.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

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


    @Bean
    public Consumer<Message<String>> delayConsume() {
        return msg -> {
            log.info(Thread.currentThread().getName() + " Delay Consumer Receive New Messages: " + msg);
        };
    }

    @Bean
    public Consumer<Message<String>> txConsumer() {
        return msg -> {
            log.info(Thread.currentThread().getName() + " Tx Consumer Receive New Messages: " + msg);
        };
    }
}
