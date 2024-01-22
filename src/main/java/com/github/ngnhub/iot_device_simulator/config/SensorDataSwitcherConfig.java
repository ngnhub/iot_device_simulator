package com.github.ngnhub.iot_device_simulator.config;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SensorDataSwitcherConfig {

    @Bean
    public ConcurrentHashMap<String, Object> topicToValue(SensorDescriptionStorage storage) {
        ConcurrentHashMap<String, Object> topicToValue = new ConcurrentHashMap<>();
        List<SensorDescription> descriptions = storage.getAll()
                .filter(SensorDescription::switcher)
                .collectList()
                .block();
        if (!CollectionUtils.isEmpty(descriptions)) {
            descriptions.forEach(description -> topicToValue.put(description.topic(), description.initValue()));
        }
        return topicToValue;
    }

    @Bean
    public ConcurrentHashMap<String, SensorDescription> topicToDescription(SensorDescriptionStorage storage) {
        ConcurrentHashMap<String, SensorDescription> topicToDescription = new ConcurrentHashMap<>();
        List<SensorDescription> descriptions = storage.getAll()
                .filter(SensorDescription::switcher)
                .collectList()
                .block();
        if (!CollectionUtils.isEmpty(descriptions)) {
            descriptions.forEach(description -> topicToDescription.put(description.topic(), description));
        }
        return topicToDescription;
    }
}
