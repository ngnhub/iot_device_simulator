package com.github.ngnhub.iot_device_simulator.model;

import lombok.Builder;

import java.time.LocalDateTime;

public record SensorData(String topic, String value, LocalDateTime time, boolean errored) {

    @Builder(toBuilder = true)
    public SensorData {}
}
