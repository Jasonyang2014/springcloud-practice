package com.auyeung.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.function.Function;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ConfigServerApplicationTest {

    @Resource
    private InputDestination inputDestination;

    @Resource
    private OutputDestination outputs;

    @Test
    public void testEmptyConfiguration() throws JsonProcessingException {
        this.inputDestination.send(new GenericMessage<>("hello".getBytes()));
        byte[] payload = outputs.receive(2000L).getPayload();
        String s = new String(payload);
        System.out.println(s);
    }

    @SpringBootApplication
    @Import(TestChannelBinderConfiguration.class)
    public static class SampleConfiguration {
        @Bean
        public Function<String, String> uppercase() {
            return v -> v.toUpperCase();
        }
    }
}
