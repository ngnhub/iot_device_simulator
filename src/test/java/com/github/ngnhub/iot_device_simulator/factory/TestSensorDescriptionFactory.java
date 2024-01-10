package com.github.ngnhub.iot_device_simulator.factory;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class TestSensorDescriptionFactory {

    public static SensorDescription gpio() {
        return new SensorDescription(
                "gpio",
                "Double",
                null,
                null,
                List.of(1.0, 0.0),
                null,
                1000L
        );
    }

    public static SensorDescription temperature() {
        return new SensorDescription(
                "temperature",
                "Double",
                -50.0,
                100.0,
                null,
                1,
                1000L
        );
    }
}
