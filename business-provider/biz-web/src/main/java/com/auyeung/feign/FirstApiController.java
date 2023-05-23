package com.auyeung.feign;

import com.auyeung.api.FirstApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/first")
public class FirstApiController implements FirstApi {

    @GetMapping("/echo/{param}")
    @Override
    public String echo(@PathVariable String param) {
        log.info("first echo {}", param);
        return String.format("Hello, %s.", param);
    }
}
