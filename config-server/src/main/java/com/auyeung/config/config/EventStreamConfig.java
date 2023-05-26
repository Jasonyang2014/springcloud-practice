package com.auyeung.config.config;

import com.auyeung.config.binding.EventStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Profile;

@Profile("stream")
@EnableBinding(EventStream.class)
public class EventStreamConfig {
}
