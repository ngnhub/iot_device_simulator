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

    public Mono<SensorData> switchOn(String topic, byte[] payload) {
        return Mono.fromSupplier(() -> switcherTopicToDescription.get(topic))
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Topic does not exist or is not switchable")))
                .flatMap(description -> convert(payload, description.type())
                        .map(value -> switchAndGet(description, value)))
                .onErrorResume((err) -> computeError(topic, (Exception) err))
                .doOnNext(publisher::publish);
    }

    private Mono<Object> convert(byte[] payload, SensorValueType type) {
        return Mono.defer(() -> type.getFromByteConverter()
                .apply(payload)
                .map(Mono::just)
                .orElseThrow(() -> new IllegalArgumentException("Can not convert consumed value. Should have type: "
                                                                        + type.getAClass())));
    }

    private SensorData switchAndGet(SensorDescription description, Object value) {
        validatePossibleValues(value, description.possibleValues());
        return create(description, value);
    }


    private void validatePossibleValues(Object value, List<Object> possibleValues) {
        if (possibleValues != null && !possibleValues.contains(value)) {
            throw new IllegalArgumentException(String.format("\"%s\" is not possible value for this topic", value));
        }
    }

    private Mono<SensorData> computeError(String switcherTopic, Exception err) {
        return Mono.fromCallable(() -> {
            log.error("Error occurred during the switching data. Topic: {}", switcherTopic, err);
            return SensorDataFactory.create(switcherTopic, err);
        });
    }
}
