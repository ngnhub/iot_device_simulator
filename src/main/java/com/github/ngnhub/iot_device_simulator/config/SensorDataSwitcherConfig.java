package com.github.ngnhub.iot_device_simulator.config;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SensorDataSwitcherConfig {

    @Bean
    public ConcurrentHashMap<String, Object> topicToValue() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentHashMap<String, SensorDescription> topicToDescription() {
        return new ConcurrentHashMap<>();
    }
}
