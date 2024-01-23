package com.github.ngnhub.iot_device_simulator.config;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class SensorDataSwitcherConfig {

    @Bean
    public Map<String, SensorDescription> switcherTopicToDescription(SensorDescriptionStorage storage) {
        return Objects.requireNonNull(storage.getAll()
                                              .filter(description -> description.switcher() != null)
                                              .collectList()
                                              .block())
                .stream()
                .collect(Collectors.toMap(SensorDescription::switcher, Function.identity()));
    }
}
