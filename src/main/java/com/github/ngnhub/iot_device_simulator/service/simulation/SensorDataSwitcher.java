package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.github.ngnhub.iot_device_simulator.error.NotFoundException;
import com.github.ngnhub.iot_device_simulator.model.ChangeDeviceValueRequest;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

// TODO: 18.01.2024 tests
@Service
@RequiredArgsConstructor
public class SensorDataSwitcher {

    private final ConcurrentHashMap<String, Object> topicToValue;
    private final ConcurrentHashMap<String, SensorDescription> topicToDescription;
    private final SensorDataPublisher publisher;

    public Mono<SensorData> switchOn(Mono<ChangeDeviceValueRequest> request) {
        return request.flatMap(changeValue -> Mono.fromCallable(() -> switchAndGet(changeValue))
                        .onErrorResume((err) -> computeError(changeValue, (Exception) err)))
                .doOnNext(publisher::publish);
    }

    private SensorData switchAndGet(ChangeDeviceValueRequest changeValue) {
        var newValue = topicToValue.compute(changeValue.topic(), (key, oldVal) -> validate(changeValue, oldVal));
        var description = topicToDescription.get(changeValue.topic());
        return convertToSensorData(description, newValue);
    }

    private Object validate(ChangeDeviceValueRequest changeValue, Object oldVal) {
        var topic = changeValue.topic();
        var newVal = changeValue.value();
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
        if (topicToDescription.containsKey(topic)) {
            SensorDescription description = topicToDescription.get(topic);
            if (description.possibleValues() != null && !description.possibleValues().contains(newVal)) {
                throw new IllegalArgumentException(String.format(
                        "\"%s\" is not possible value for this topic", newVal));
            }
        }
    }

    // TODO: 18.01.2024 mupstucr
    private SensorData convertToSensorData(SensorDescription description, Object value) {
        String unitOfMeasure = description.unitOfMeasure();
        return SensorData.builder().topic(description.topic())
                .sensorData(value.toString() + (unitOfMeasure == null ? "" : unitOfMeasure))
                .time(LocalDateTime.now())
                .build();
    }

    private Mono<SensorData> computeError(ChangeDeviceValueRequest changeValue, Exception err) {
        return Mono.fromCallable(() -> getErrorData(changeValue.topic(), err));
    }

    private SensorData getErrorData(String topic, Exception exc) {
        return SensorData.builder().topic(topic)
                .sensorData("Error {" + exc.getMessage() + "}")
                .time(LocalDateTime.now())
                .errored(true)
                .build();
    }
}