package com.github.ngnhub.iot_device_simulator.service.simulation.consuming;

import com.github.ngnhub.iot_device_simulator.error.NotFoundException;
import com.github.ngnhub.iot_device_simulator.mapper.SensorDataFactory;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.mapper.SensorDataFactory.create;

@Service
@RequiredArgsConstructor
public class SensorDataSwitcher {

    private final ConcurrentHashMap<String, Object> topicToSwitchableValue;
    private final ConcurrentHashMap<String, SensorDescription> topicToDescriptionOfSwitchable;

    public Mono<SensorData> switchOn(String topic, Object value) {
        return Mono.fromCallable(() -> switchAndGet(topic, value))
                .onErrorResume((err) -> computeError(topic, (Exception) err));
    }

    private SensorData switchAndGet(String topic, Object value) {
        var newValue = topicToSwitchableValue.compute(topic, (key, oldVal) -> validate(topic, value, oldVal));
        var description = topicToDescriptionOfSwitchable.get(topic);
        return create(description, newValue);
    }

    private Object validate(String topic, Object newVal, Object oldVal) {
        validateExist(oldVal);
        validateType(oldVal, newVal);
        validatePossibleValues(topic, newVal);
        return newVal;
    }

    private void validateExist(Object oldVal) {
        if (oldVal == null) {
            throw new NotFoundException("Topic does not exist or is not switchable");
        }
    }

    private void validateType(Object oldVal, Object newVal) {
        if (!oldVal.getClass().isInstance(newVal)) {
            throw new IllegalArgumentException("Incompatible type: " + newVal.getClass().getSimpleName());
        }
    }

    private void validatePossibleValues(String topic, Object newVal) {
        if (topicToDescriptionOfSwitchable.containsKey(topic)) {
            SensorDescription description = topicToDescriptionOfSwitchable.get(topic);
            if (description.possibleValues() != null && !description.possibleValues().contains(newVal)) {
                throw new IllegalArgumentException(String.format(
                        "\"%s\" is not possible value for this topic", newVal));
            }
        }
    }

    private Mono<SensorData> computeError(String topic, Exception err) {
        return Mono.fromCallable(() -> SensorDataFactory.create(topic, err));
    }
}
