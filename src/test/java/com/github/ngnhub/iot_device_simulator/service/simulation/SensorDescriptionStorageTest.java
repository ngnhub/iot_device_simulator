package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.temperature;

class SensorDescriptionStorageTest extends BaseTest {

    private SensorDescriptionStorage sensorDescriptionStorage;

    @BeforeEach
    void setUp() throws IOException {
        var file = ResourceUtils.getFile("classpath:test_sensors.json");
        var mapper = new ObjectMapper();
        List<SensorDescription> descriptionList = mapper.readValue(file, new TypeReference<>() {});
        Map<String, SensorDescription> descriptionMap = descriptionList.stream()
                .collect(Collectors.toMap(SensorDescription::topic, Function.identity()));
        sensorDescriptionStorage = new SensorDescriptionStorage(descriptionMap);
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

    @Test
    void shouldReturnOnlySwitchable() {
        // when
        var actual = sensorDescriptionStorage.getOnlySwitchable();

        // then
        var fan = fan();
        StepVerifier.create(actual)
                .expectNext(fan)
                .verifyComplete();
    }
}
