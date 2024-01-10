package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.utils.SensorDescriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.temperature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SensorDescriptionStorageTest {

    @Mock
    private SensorDescriptionValidator validator;
    private SensorDescriptionStorage sensorDescriptionStorage;

    @BeforeEach
    void setUp() throws IOException {
        sensorDescriptionStorage = new SensorDescriptionStorage("classpath:test_sensors.json", validator);
    }

    @Test
    void shouldReturnGpioDescription() {
        // given
        var expected = gpio();

        // when
        var actual = sensorDescriptionStorage.getBy("gpio").block();

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldThrowsWhenSensorDoesNotExist() {
        // when
        var exc =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> sensorDescriptionStorage.getBy("not existed").block()
                );

        // then
        assertEquals("Sensor is not found: not existed", exc.getMessage());
    }

    @Test
    void shouldReturnAllFromJsonFile() {
        // when
        var actual = sensorDescriptionStorage.getAll();

        // then
        var gpio = gpio();
        var temperature = temperature();
        StepVerifier.create(actual)
                .expectNext(temperature)
                .expectNext(gpio)
                .verifyComplete();
    }

    @Test
    void shouldReturnAllTopics() {
        // when
        var actual = sensorDescriptionStorage.getAllTopics();

        // then
        StepVerifier.create(actual)
                .expectNext("temperature")
                .expectNext("gpio")
                .verifyComplete();
    }
}