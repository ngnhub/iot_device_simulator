package com.github.ngnhub.iot_device_simulator.factory;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class TestSensorDataFactory {

    public static final LocalDateTime TEST_SENSOR_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
    public static final int TEST_QOS = 2;

    public static SensorData getSensorData(String topic, String value) {
        return SensorData.builder()
                .topic(topic)
                .sensorData(value)
                .time(TEST_SENSOR_TIME)
                .qos(TEST_QOS)
                .build();
    }
}
