package com.github.ngnhub.iot_device_simulator.model;

import lombok.Builder;
import lombok.Data;

import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * @param <T> - sensor's value type // TODO: 10.01.2024 do i need it?
 */
@Data
@Builder
public class SensorData<T> {

    private String topic;
    private T sensorData;
    private LocalDateTime time;
    @Nullable
    private Integer qos;
}
