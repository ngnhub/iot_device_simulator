package com.github.ngnhub.iot_device_simulator.utils;

import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

public enum SensorValueType {

    DOUBLE(Double.class, bytes -> {
        try {
            String value = new String(bytes, StandardCharsets.UTF_8);
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }),
    STRING(String.class, bytes -> {
        try {
            String value = new String(bytes, StandardCharsets.UTF_8);
            return Optional.of(value);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    });

    @Getter
    private final Class<?> aClass;

    @Getter
    private final Function<byte[], Optional<Object>> fromByteConverter;

    SensorValueType(Class<?> typeSimpleClassName, Function<byte[], Optional<Object>> fromByteConverter) {
        this.aClass = typeSimpleClassName;
        this.fromByteConverter = fromByteConverter;
    }
}
