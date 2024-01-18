package com.github.ngnhub.iot_device_simulator.model;

public record ChangeDeviceValueRequest(String topic, Object value) {
}
