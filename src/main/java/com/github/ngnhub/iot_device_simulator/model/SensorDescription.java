package com.github.ngnhub.iot_device_simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ngnhub.iot_device_simulator.utils.SensorValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import org.springframework.lang.Nullable;

import java.util.List;

public record SensorDescription(@NotBlank String topic,
                                @NotNull SensorValueType type,
                                @Nullable String unitOfMeasure,
                                @Nullable Double min,
                                @Nullable Double max,
                                @Nullable List<Object> possibleValues,
                                @Nullable Integer qos,
                                @JsonProperty("intervalInMillis") Long interval,
                                @Nullable String switcher) {

    @Builder(toBuilder = true)
    public SensorDescription {}
}
