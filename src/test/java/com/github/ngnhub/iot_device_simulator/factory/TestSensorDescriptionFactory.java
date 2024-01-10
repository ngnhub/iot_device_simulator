package com.github.ngnhub.iot_device_simulator.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.experimental.UtilityClass;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@UtilityClass
public class TestSensorDescriptionFactory {
    private static final List<SensorDescription> DESCRIPTIONS;

    static {
        try {
            File file = ResourceUtils.getFile("classpath:test_sensors.json");
            var mapper = new ObjectMapper();
            DESCRIPTIONS = mapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SensorDescription gpio() {
        return DESCRIPTIONS.get(0);
    }

    public static SensorDescription temperature() {
        return DESCRIPTIONS.get(1);
    }
}
