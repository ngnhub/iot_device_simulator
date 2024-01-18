package com.github.ngnhub.iot_device_simulator.service.simulation.impl;

import com.github.ngnhub.iot_device_simulator.error.NotFoundException;
import com.github.ngnhub.iot_device_simulator.model.ChangeDeviceValueRequest;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataSwitcher;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

// TODO: 18.01.2024 tests
@Service
public class SensorDataSwitcherImpl implements SensorDataSwitcher {

    private final ConcurrentHashMap<String, Object> topicToValue;
    private final ConcurrentHashMap<String, SensorDescription> topicToDescription;
    private final SensorDataPublisher publisher;

    private SensorDataSwitcherImpl(SensorDescriptionStorage storage,
                                   ConcurrentHashMap<String, Object> topicToValue,
                                   ConcurrentHashMap<String, SensorDescription> topicToDescription,
                                   SensorDataPublisher publisher) {
        this.topicToValue = topicToValue;
        this.topicToDescription = topicToDescription;
        this.publisher = publisher;
        storage.getAll()
                .filter(SensorDescription::switcher)
                .subscribe(description -> {
                    this.topicToValue.put(description.topic(), description.initValue());
                    this.topicToDescription.put(description.topic(), description);
                });
    }

    @Override
    public Mono<SensorData> switchOn(Mono<ChangeDeviceValueRequest> request) {
        return request.flatMap(changeValue -> Mono.fromCallable(() -> setAndGet(changeValue))
                        .onErrorResume((err) -> Mono.just(getErrorData(
                                topicToDescription.get(changeValue.topic()),
                                (Exception) err
                        ))))
                .doOnNext(publisher::publish);
    }

    private SensorData setAndGet(ChangeDeviceValueRequest changeValue) {
        var newValue = topicToValue.compute(changeValue.topic(), (key, oldVal) -> validate(changeValue, oldVal));
        var description = topicToDescription.get(changeValue.topic());
        return convertToSensorData(description, newValue);
    }

    private Object validate(ChangeDeviceValueRequest changeValue, Object oldVal) {
        var topic = changeValue.topic();
        var newVal = changeValue.value();
        validateExist(oldVal, topic);
        validateType(oldVal, newVal);
        validatePossibleValues(topic, newVal);
        return newVal;
    }

    private void validateExist(Object oldVal, String topic) {
        if (oldVal == null) {
            throw new NotFoundException("Topic {" + topic + "} is not existed or not switchable");
        }
    }

    private void validateType(Object oldVal, Object newVal) {
        if (!oldVal.getClass().isInstance(newVal)) {
            throw new IllegalArgumentException("Incompatible type: " + oldVal.getClass());
        }
    }

    private void validatePossibleValues(String topic, Object newVal) {
        if (topicToDescription.containsKey(topic)) {
            SensorDescription description = topicToDescription.get(topic);
            if (description.possibleValues() != null && description.possibleValues().contains(newVal)) {
                throw new IllegalArgumentException(String.format(
                        "{%s} is not possible value for the t opic %s",
                        newVal,
                        topic
                ));
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

    private SensorData getErrorData(SensorDescription description, Exception exc) {
        return SensorData.builder().topic(description.topic())
                .sensorData("Error {" + exc.getMessage() + "}")
                .time(LocalDateTime.now())
                .errored(true)
                .build();
    }
}
