package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SensorDescriptionStorage {

    private final Map<String, SensorDescription> descriptions;

    public Flux<SensorDescription> getAll() {
        return Flux.fromIterable(descriptions.values());
    }

    public Flux<SensorDescription> getOnlySwitchable() {
        return getAll()
                .filter(description -> description.switcher() != null);
    }
}
