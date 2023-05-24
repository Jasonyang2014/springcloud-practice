package com.auyeung.feign;

import com.auyeung.api.SecondApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/second/")
public class SecondApiController implements SecondApi {

    @GetMapping("/sleep/{times}")
    @Override
    public String sleep(@PathVariable String times) {
        int i = Integer.parseInt(times);
        try {
            log.info("second api sleep {}", i);
            TimeUnit.SECONDS.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return String.format("sleep %s", times);
    }
}
