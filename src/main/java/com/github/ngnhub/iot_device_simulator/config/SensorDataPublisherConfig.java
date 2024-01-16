package com.github.ngnhub.iot_device_simulator.config;

import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SensorDataPublisherConfig {

    @Bean
    public ConcurrentHashMap<String, Map<String, SensorDataPublisher.DataConsumer>> topicToMessageQueues() {
        return new ConcurrentHashMap<>();
    }
}
