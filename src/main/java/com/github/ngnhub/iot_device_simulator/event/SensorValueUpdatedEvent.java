package com.github.ngnhub.iot_device_simulator.event;

import com.github.ngnhub.iot_device_simulator.model.SensorData;

public record SensorValueUpdatedEvent(SensorData data) {
}
