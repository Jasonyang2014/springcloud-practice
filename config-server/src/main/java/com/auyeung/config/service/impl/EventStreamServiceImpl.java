package com.auyeung.config.service.impl;

import com.auyeung.config.binding.EventStream;
import com.auyeung.config.service.EventStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Profile("stream")
@Service
public class EventStreamServiceImpl implements EventStreamService {

    @Autowired
    private EventStream eventStream;

    @Override
    public Boolean produceEvent(String parsm) {
        System.out.println("Producing events --> id: " + parsm);
        MessageChannel messageChannel = eventStream.producer();
        return messageChannel.send(MessageBuilder.withPayload(parsm)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN_VALUE).build());
    }
}
