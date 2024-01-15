package com.github.ngnhub.iot_device_simulator.config;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import reactor.core.publisher.Sinks;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SensorDataPublisherConfig {

    @Bean
    public Map<String, Sinks.Many<SensorData>> topicToMessageQueues() {
        return new ConcurrentHashMap<>();
    }
}
