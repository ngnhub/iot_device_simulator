package com.github.ngnhub.iot_device_simulator.service.simulation.consuming;

import com.github.ngnhub.iot_device_simulator.error.NotFoundException;
import com.github.ngnhub.iot_device_simulator.mapper.SensorDataFactory;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.utils.SensorValueType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.github.ngnhub.iot_device_simulator.mapper.SensorDataFactory.create;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataSwitcher {

    private final Map<String, SensorDescription> switcherTopicToDescription;
    private final SensorDataPublisher publisher;

    public Mono<SensorData> switchOn(String topic, Object value) {
        return Mono.fromCallable(() -> switchAndGet(topic, value))
                .onErrorResume((err) -> computeError(topic, (Exception) err))
                .doOnNext(publisher::publish);
    }

    private SensorData switchAndGet(String switchTopic, Object value) {
        var description = switcherTopicToDescription.get(switchTopic);
        validate(description, value);
        return create(description, value);
    }

    private void validate(SensorDescription description, Object value) {
        validateExist(description);
        validateType(value, description.type());
        validatePossibleValues(value, description.possibleValues());
    }

    private void validateExist(SensorDescription description) {
        if (description == null) {
            throw new NotFoundException("Topic does not exist or is not switchable");
        }
    }

    private void validateType(Object value, SensorValueType type) {
        if (!type.getAClass().isInstance(value)) {
            throw new IllegalArgumentException("Incompatible type: " + value.getClass().getSimpleName());
        }
    }

    private void validatePossibleValues(Object value, List<Object> possibleValues) {
        if (possibleValues != null && !possibleValues.contains(value)) {
            throw new IllegalArgumentException(String.format("\"%s\" is not possible value for this topic", value));
        }
    }

    private Mono<SensorData> computeError(String switcherTopic, Exception err) {
        return Mono.fromCallable(() -> {
            log.error("Error occurred during the switching data. Topic: {}", switcherTopic, err);
            String sensorTopic = switcherTopicToDescription.get(switcherTopic).topic();
            return SensorDataFactory.create(sensorTopic, err);
        });
    }
}
