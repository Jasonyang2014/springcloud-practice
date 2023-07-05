package com.auyeung.stream.controller;

import com.auyeung.stream.service.MsgService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequestMapping("/msg")
@RestController
public class MsgController {

    private final MsgService msgService;

    @GetMapping("/send/{msg}")
    public int send(@PathVariable String msg) {
        return msgService.sendMsg(msg);
    }


    @GetMapping("/delay/{msg}")
    public boolean delaySend(@PathVariable String msg) {
        return msgService.sendDelayMsg(msg);
    }

    @GetMapping("/tx/{msg}")
    public Boolean txSend(@PathVariable String msg) {
        return msgService.sendTxMsg(msg);
    }
}
