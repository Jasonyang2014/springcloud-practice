package com.auyeung.stream.service.impl;

import com.auyeung.stream.service.MsgService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Slf4j
@Service
public class MsgServiceImpl implements MsgService {

    private final StreamBridge streamBridge;

    @Override
    public int sendMsg(String msg) {
        MessageBuilder<String> builder = MessageBuilder.withPayload(msg)
                .setHeader(RocketMQHeaders.TAGS, "binder")
                .setHeader(RocketMQHeaders.KEYS, "my-test-key")
                .setHeader(MessageConst.PROPERTY_DELAY_TIME_LEVEL, "1");
        Message<String> message = builder.build();
        boolean send = streamBridge.send("rocketmq-out-0", message);
        return send ? 1 : 0;
    }
}
