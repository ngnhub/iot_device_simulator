package com.github.ngnhub.iot_device_simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;

import org.springframework.lang.Nullable;

import java.util.List;

//TODO: 07.01.2024 jakarta valid
public record SensorDescription(String topic,
                                @Pattern(regexp = "^(double|string)$")
                                String type,
                                @Nullable Double max,
                                @Nullable Double min,
                                @Nullable List<Object> possibleValues, /*todo rename this field, not only for strings*/
                                @Nullable Integer qos,
                                @JsonProperty("intervalInMillis") Long interval) {
}
