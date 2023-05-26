package com.auyeung.config.controller;

import com.auyeung.config.service.EventStreamService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
public class TestController {

    private final StreamBridge streamBridge;

    @GetMapping("/send/{bindingName}/{param}")
    public String send(@PathVariable(name = "param") String param,
                       @PathVariable(name = "bindingName") String bindingName) {
        boolean input = streamBridge.send(bindingName, MessageBuilder.withPayload(param).build());
        if (input) {
            log.info("send message {}", param);
            return "send " + param;
        } else {
            return "send error";
        }
    }
}
