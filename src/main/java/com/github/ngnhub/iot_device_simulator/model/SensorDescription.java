package com.github.ngnhub.iot_device_simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ngnhub.iot_device_simulator.utils.SensorValueTypes;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import org.springframework.lang.Nullable;

import java.util.List;

// TODO: 10.01.2024 unit of measure
public record SensorDescription(String topic,
                                SensorValueTypes type,
                                @Nullable Double min,
                                @Nullable Double max,
                                @Nullable List<Object> possibleValues,
                                @Nullable Integer qos,
                                @NotNull @JsonProperty("intervalInMillis") Long interval) {

    @Builder(toBuilder = true)
    public SensorDescription {}
}
