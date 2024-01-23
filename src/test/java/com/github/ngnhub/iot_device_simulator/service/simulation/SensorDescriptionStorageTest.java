package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.utils.SensorDescriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import reactor.test.StepVerifier;

import java.io.IOException;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.temperature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensorDescriptionStorageTest extends BaseTest {

    @Mock
    private SensorDescriptionValidator validator;
    private SensorDescriptionStorage sensorDescriptionStorage;

    @BeforeEach
    void setUp() throws IOException {
        sensorDescriptionStorage = new SensorDescriptionStorage("classpath:test_sensors.json", validator);
    }

    @Test
    void shouldReturnAllFromJsonFile() {
        // when
        var actual = sensorDescriptionStorage.getAll();

        // then
        var gpio = gpio();
        var temperature = temperature();
        var fan = fan();
        StepVerifier.create(actual)
                .expectNext(fan)
                .expectNext(temperature)
                .expectNext(gpio)
                .verifyComplete();
    }
}