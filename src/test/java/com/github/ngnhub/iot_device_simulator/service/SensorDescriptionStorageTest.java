package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensorDescriptionStorageTest {

    @Test
    void shouldReturnGpioDescription() throws IOException {
        // given
        SensorDescriptionStorage sensorDescriptionStorage = new SensorDescriptionStorage("classpath:test_sensors.json");
        SensorDescription expected = new SensorDescription("gpio", "double", 1.0, 0.0, null, 1000L);

        // when
        SensorDescription actual = sensorDescriptionStorage.getBy("gpio").block();

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowsWhenSensorDoesNotExist() throws IOException {
        // given
        SensorDescriptionStorage sensorDescriptionStorage = new SensorDescriptionStorage("classpath:test_sensors.json");

        // when
        IllegalArgumentException exc =
                assertThrows(IllegalArgumentException.class, () -> sensorDescriptionStorage.getBy("not existed").block());

        // then
        assertEquals("Sensor is not found: not existed", exc.getMessage());
    }
}