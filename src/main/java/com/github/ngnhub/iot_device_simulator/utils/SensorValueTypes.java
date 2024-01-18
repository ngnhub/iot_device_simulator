package com.github.ngnhub.iot_device_simulator.utils;

import lombok.Getter;

public enum SensorValueTypes {

    DOUBLE(Double.class.getSimpleName()),
    STRING(String.class.getSimpleName());

    @Getter
    private final String typeSimpleClassName;

    SensorValueTypes(String typeSimpleClassName) {
        this.typeSimpleClassName = typeSimpleClassName;
    }
}
