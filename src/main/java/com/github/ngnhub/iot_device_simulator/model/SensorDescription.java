package com.github.ngnhub.iot_device_simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ngnhub.iot_device_simulator.utils.SensorValueTypes;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import org.springframework.lang.Nullable;

import java.util.List;

public record SensorDescription(String topic,
                                SensorValueTypes type,
                                @Nullable String unitOfMeasure,
                                @Nullable Double min,
                                @Nullable Double max,
                                @Nullable List<Object> possibleValues,
                                @Nullable Integer qos,
                                @NotNull @JsonProperty("intervalInMillis") Long interval,
                                boolean switcher,
                                Object initValue) {

    @Builder(toBuilder = true)
    public SensorDescription {}
}
