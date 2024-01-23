package com.github.ngnhub.iot_device_simulator.config;


import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher.SensorDataListener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SensorDataPublisherConfig {

    @Bean
    public ConcurrentHashMap<String, ConcurrentHashMap<String, SensorDataListener>> topicToMessageListeners(
            SensorDescriptionStorage storage) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, SensorDataListener>> listener = new ConcurrentHashMap<>();
        List<SensorDescription> descriptions = storage.getAll()
                .collectList()
                .block();
        if (descriptions != null) {
            descriptions.forEach(description -> listener.put(description.topic(), new ConcurrentHashMap<>()));
        }
        return listener;
    }
}
