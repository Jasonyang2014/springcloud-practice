package com.auyeung.config.consumer;

import com.auyeung.config.binding.EventStream;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;

@Profile("stream")
@Configuration
public class EventConsumer {

    @StreamListener(EventStream.INBOUND)
    public void consumeEvent(@Payload String msg) {
        System.out.println("receive message " + msg);
    }
}
