package com.github.ngnhub.iot_device_simulator.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder // TODO: 22.01.2024 record
public class SensorData {

    private String topic;
    private String value;
    private LocalDateTime time;
    private boolean errored;
}
