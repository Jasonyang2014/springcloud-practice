package com.auyeung.config.binding;


import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.MessageChannel;

public interface EventStream {
    String INBOUND = "input";
    String OUTBOUND = "output";

    @Input(INBOUND)
    SubscribableChannel consumer();

    @Output(OUTBOUND)
    MessageChannel producer();
}
