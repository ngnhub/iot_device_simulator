package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.utils.SensorDescriptionValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: 14.01.2024 allow turn on / turn off types
@Service
public class SensorDescriptionStorage {

    private final Map<String, SensorDescription> descriptions;

    public SensorDescriptionStorage(@Value("${sensor-descriptor-path}") String descriptorPath,
                                    SensorDescriptionValidator validator) throws IOException {
        var file = ResourceUtils.getFile(descriptorPath);
        var mapper = new ObjectMapper();
        List<SensorDescription> descriptionList = mapper.readValue(file, new TypeReference<>() {});
        descriptionList.forEach(validator::validate);
        descriptions = descriptionList.stream()
                .collect(Collectors.toMap(SensorDescription::topic, Function.identity()));
    }

    public Flux<SensorDescription> getAll() {
        return Flux.fromIterable(descriptions.values());
    }

    /**
     * No need to utilize mono here, just for simulating kind of blocking storage
     */
    public Mono<SensorDescription> getBy(String name) {
        SensorDescription description = descriptions.get(name);
        if (description == null) {
            throw new IllegalArgumentException("Sensor is not found: " + name);
        }
        return Mono.just(description);
    }
}
