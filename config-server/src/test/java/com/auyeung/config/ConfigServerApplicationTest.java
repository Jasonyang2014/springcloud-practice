package com.auyeung.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ConfigServerApplicationTest {

    @Resource
    private InputDestination input;

    @Resource
    private OutputDestination output;

    @Test
    public void testEmptyConfiguration() throws JsonProcessingException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("info", "hello");
        ObjectMapper objectMapper = new ObjectMapper();
        String s1 = objectMapper.writeValueAsString(map);
        this.input.send(MessageBuilder.withPayload(s1).build());
        byte[] payload = output.receive(2000L).getPayload();
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