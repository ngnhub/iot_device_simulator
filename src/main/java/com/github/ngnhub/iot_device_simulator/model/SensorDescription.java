package com.github.ngnhub.iot_device_simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import org.springframework.lang.Nullable;

import java.util.List;

// TODO: 10.01.2024 unit of measure
public record SensorDescription(String topic,
                                @Pattern(regexp = "^(Double|String)$")
                                String type,
                                @Nullable Double min,
                                @Nullable Double max,
                                @Nullable List<Object> possibleValues,
                                @Nullable Integer qos,
                                @NotNull @JsonProperty("intervalInMillis") Long interval) {

    @Builder(toBuilder = true)
    public SensorDescription {}
}
