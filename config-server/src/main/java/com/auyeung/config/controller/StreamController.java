package com.auyeung.config.controller;

import com.auyeung.config.service.EventStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Profile("stream")
@RestController
public class StreamController {

    @Autowired
    private EventStreamService eventStreamService;

    @GetMapping("/produce/{param}")
    public Boolean sendEvent(@PathVariable String param) {
        return eventStreamService.produceEvent(param);
    }
}
