package com.github.ngnhub.iot_device_simulator.model;

import lombok.Builder;
import lombok.Data;

import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Data
@Builder
public class SensorData {

    private String topic;
    private String sensorData;
    private LocalDateTime time;
    @Nullable
    private Integer qos;
    private boolean errored;
}
