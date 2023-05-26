package com.auyeung.config.func;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Profile("func")
@Configuration
public class FuncBinding {


    /**
     * 绑定的消费渠道
     * @return
     */
    @Bean
    public Function<String, String> output() {
        return s -> {
            String upperCase = s.toUpperCase();
            log.info("out put {}", upperCase);
            return upperCase;
        };
    }

    @Bean
    public Consumer<String> input() {
        return x -> log.info("input receive msg {}", x);
    }
}
