package com.auyeung.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bus.StreamBusBridge;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import({ConfigClientApplicationTest.TestChannelBinderConfiguration.class})
class ConfigClientApplicationTest {



    @Test
    void contextLoads() {


    }


    @Test
    public void test() {

    }

    @Configuration
    @EnableAutoConfiguration
    static class TestChannelBinderConfiguration{

    }
}
