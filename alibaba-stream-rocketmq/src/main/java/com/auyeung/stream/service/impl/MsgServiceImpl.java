package com.auyeung.stream.service.impl;

import com.auyeung.stream.service.MsgService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .setHeader(RocketMQHeaders.DELAY, "1");
        Message<String> message = builder.build();
        boolean send = streamBridge.send("rocketmq-out-0", message);
        return send ? 1 : 0;
    }

    /**
     * 发送延时消息
     * 4.x版本不支持自定义延时，delay参数参考
     * <a href="https://rocketmq.apache.org/zh/docs/4.x/producer/04message3">延时参数说明</a><br/>
     * 创建延时主题<br/>
     * <code>
     * /bin/mqadmin updateTopic -c DefaultCluster -t DelayTopic -n 127.0.0.1:9876 -a +message.type=DELAY
     * </code>
     *
     * @param msg 消息
     */
    @Override
    public boolean sendDelayMsg(String msg) {
        //定时/延时消息发送
        //https://rocketmq.apache.org/zh/docs/4.x/producer/04message3
        MessageBuilder<String> builder = MessageBuilder.withPayload(msg)
                .setHeader(RocketMQHeaders.TOPIC, "delay-order")
                //delay
                .setHeader(RocketMQHeaders.DELAY, "4")
                .setHeader(RocketMQHeaders.TAGS, "delay")
                .setHeader(RocketMQHeaders.KEYS, "delay-key");
        Message<String> message = builder.build();
        log.info("send delay message {}", message);
        return streamBridge.send("delay-out-0", message);
    }

    /**
     * 发送事务信息
     *
     * @param msg 消息
     * @return
     */
    @Override
    public boolean sendTxMsg(String msg) {
        MessageBuilder<String> builder = MessageBuilder.withPayload(msg)
                .setHeader(RocketMQHeaders.TOPIC, "tx-order")
                .setHeader(RocketMQHeaders.DELAY, "1")
                .setHeader(RocketMQHeaders.TAGS, "tx")
                .setHeader("test", msg)
                .setHeader(RocketMQHeaders.KEYS, "tx-key");
        Message<String> message = builder.build();
        return streamBridge.send("tx-out-0", message);
    }


}
