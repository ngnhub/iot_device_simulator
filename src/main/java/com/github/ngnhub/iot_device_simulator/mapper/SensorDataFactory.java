package com.github.ngnhub.iot_device_simulator.mapper;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class SensorDataFactory {

    public static SensorData create(SensorDescription description, Object value) {
        String unitOfMeasure = description.unitOfMeasure();
        return SensorData.builder().topic(description.topic())
                .value(value.toString() + (unitOfMeasure == null ? "" : unitOfMeasure))
                .time(LocalDateTime.now())
                .build();
    }

    public static SensorData create(String topic, Exception exc) {
        return SensorData.builder().topic(topic)
                .value("Error occurred during the data processing { " + exc.getMessage() + " }")
                .time(LocalDateTime.now())
                .errored(true)
                .build();
    }
}
