package com.github.ngnhub.iot_device_simulator.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.List;

@UtilityClass
public class TestSensorDescriptionFactory {

    public static SensorDescription gpio() {
        return getSensorDescription(0);
    }

    public static SensorDescription temperature() {
        return getSensorDescription(1);
    }

    public static SensorDescription fan() {
        return getSensorDescription(2);
    }

    @SneakyThrows
    private synchronized static SensorDescription getSensorDescription(int index) {
        File file = ResourceUtils.getFile("classpath:test_sensors.json");
        var mapper = new ObjectMapper();
        List<SensorDescription> values = mapper.readValue(file, new TypeReference<>() {});
        return values.get(index);
    }
}
