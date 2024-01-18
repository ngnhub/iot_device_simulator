package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataSimulator {

    private final SensorDescriptionStorage storage;
    private final SensorDataPublisher publisher;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        startGenerateValues();
    }

    public void startGenerateValues() {
        storage.getAll()
                .flatMap(description -> Flux
                        .interval(Duration.ofMillis(description.interval()))
                        .map(v -> tryGetRandomValue(description))
                )
                .subscribe(publisher::publish);
    }

    private SensorData tryGetRandomValue(SensorDescription description) {
        try {
            return getRandomValue(description);
        } catch (Exception e) {
            return getErrorData(description, e);
        }
    }

    private SensorData getRandomValue(SensorDescription description) {
        switch (description.type()) {
            case DOUBLE -> {
                return getRandomPossibleOrBoundedValue(description);
            }
            case STRING -> {
                return getRandomPossibleValue(description);
            }
            default -> throw new UnsupportedOperationException("Unsupported type: " + description.type());
        }
    }

    private SensorData getRandomPossibleOrBoundedValue(SensorDescription description) {
        if (!CollectionUtils.isEmpty(description.possibleValues())) {
            return getRandomPossibleValue(description);
        }
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        if (description.min() != null) {
            min = description.min();
        }
        if (description.max() != null) {
            max = description.max();
        }
        double value = min + new Random().nextDouble() * (max - min);
        return convertToSensorData(description, value);
    }

    private SensorData getRandomPossibleValue(SensorDescription description) {
        List<?> possibleStringValues = description.possibleValues();
        if (CollectionUtils.isEmpty(possibleStringValues)) {
            throw new IllegalArgumentException("There is no possible values for: " + description.topic());
        }
        int index = new Random().nextInt(possibleStringValues.size());
        var value = possibleStringValues.get(index);
        return convertToSensorData(description, value);
    }

    private SensorData convertToSensorData(SensorDescription description, Object value) {
        String unitOfMeasure = description.unitOfMeasure();
        return SensorData.builder().topic(description.topic())
                .sensorData(value.toString() + (unitOfMeasure == null ? "" : unitOfMeasure))
                .time(LocalDateTime.now())
                .qos(description.qos())
                .build();
    }

    private SensorData getErrorData(SensorDescription description, Exception exc) {
        return SensorData.builder().topic(description.topic())
                .sensorData("Error {" + exc.getMessage() + "}")
                .time(LocalDateTime.now())
                .qos(description.qos())
                .errored(true)
                .build();
    }
}
