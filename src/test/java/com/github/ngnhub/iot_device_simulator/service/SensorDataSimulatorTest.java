package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataSimulator;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.test.scheduler.VirtualTimeScheduler;

import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.temperature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SensorDataSimulatorTest extends BaseTest {

    private static final VirtualTimeScheduler SCHEDULER = VirtualTimeScheduler.getOrSet();

    @Mock
    private SensorDescriptionStorage storage;
    @Mock
    private SensorDataPublisher datPublisher;
    @Captor
    private ArgumentCaptor<SensorData> eventArgumentCaptor;
    private SensorDataSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new SensorDataSimulator(storage, datPublisher);
    }

    @AfterAll
    static void afterAll() {
        SCHEDULER.dispose();
    }

    @Test
    void shouldGenerateDoubleValuesBetweenZeroAndFive() {
        // given
        SensorDescription gpio = gpio();
        SensorDescription temperature = temperature();
        Flux<SensorDescription> fux = Flux.just(gpio, temperature);
        when(storage.getAll()).thenReturn(fux);
        assertTrue(temperature.interval() > gpio.interval());
        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(datPublisher, times(2)).publish(eventArgumentCaptor.capture());
        List<SensorData> allValues = eventArgumentCaptor.getAllValues();
        assertEquals(2, allValues.size());

        SensorData gpioCaptured = allValues.get(0);
        assertEquals(gpio.topic(), gpioCaptured.getTopic());
        assertFalse(CollectionUtils.isEmpty(gpio.possibleValues()));
        assertTrue(gpio.possibleValues().contains(gpioCaptured.getSensorData()));

        SensorData temperatureCaptured = allValues.get(1);
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());

        String capturedTemperatureData = temperatureCaptured.getSensorData();
        assertTrue(capturedTemperatureData.endsWith(temperature.unitOfMeasure()));
        capturedTemperatureData = capturedTemperatureData.replace("C", "");
        double sensorData = Double.parseDouble(capturedTemperatureData);
        assertNotNull(temperature.min());
        assertNotNull(temperature.max());
        boolean inRange = temperature.min() <= sensorData && sensorData <= temperature.max();
        assertTrue(inRange);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenMinDoubleAndFive() {
        // given
        SensorDescription temperature = temperature()
                .toBuilder()
                .min(null)
                .build();
        when(storage.getAll()).thenReturn(Flux.just(temperature));

        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(datPublisher).publish(eventArgumentCaptor.capture());
        SensorData temperatureCaptured = eventArgumentCaptor.getValue();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());

        String capturedTemperatureData = temperatureCaptured.getSensorData();
        assertTrue(capturedTemperatureData.endsWith(temperature.unitOfMeasure()));
        capturedTemperatureData = capturedTemperatureData.replace("C", "");
        double sensorData = Double.parseDouble(capturedTemperatureData);
        assertNull(temperature.min());
        assertNotNull(temperature.max());
        boolean inRange = Double.MIN_VALUE <= sensorData && sensorData <= temperature.max();
        assertTrue(inRange);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenZeroAndMaxDouble() {
        // given
        SensorDescription temperature = temperature()
                .toBuilder()
                .max(null)
                .build();
        when(storage.getAll()).thenReturn(Flux.just(temperature));

        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(datPublisher).publish(eventArgumentCaptor.capture());
        SensorData temperatureCaptured = eventArgumentCaptor.getValue();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());

        String capturedTemperatureData = temperatureCaptured.getSensorData();
        assertTrue(capturedTemperatureData.endsWith(temperature.unitOfMeasure()));
        capturedTemperatureData = capturedTemperatureData.replace("C", "");
        double sensorData = Double.parseDouble(capturedTemperatureData);
        assertNotNull(temperature.min());
        assertNull(temperature.max());
        boolean inRange = temperature.min() <= sensorData && sensorData <= Double.MAX_VALUE;
        assertTrue(inRange);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenMinAndMaxDouble() {
        // given
        SensorDescription temperature = temperature()
                .toBuilder()
                .min(null)
                .max(null)
                .build();
        when(storage.getAll()).thenReturn(Flux.just(temperature));

        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(datPublisher).publish(eventArgumentCaptor.capture());
        SensorData temperatureCaptured = eventArgumentCaptor.getValue();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());

        String capturedTemperatureData = temperatureCaptured.getSensorData();
        assertTrue(capturedTemperatureData.endsWith(temperature.unitOfMeasure()));
        capturedTemperatureData = capturedTemperatureData.replace("C", "");
        double sensorData = Double.parseDouble(capturedTemperatureData);
        assertNull(temperature.min());
        assertNull(temperature.max());
        boolean inRange = Double.MIN_VALUE <= sensorData && sensorData <= Double.MAX_VALUE;
        assertTrue(inRange);
    }

    @Test
    void shouldPGenerateErrorMessageWhenNoPossibleValuesPresent() {
        // given
        SensorDescription gpio = gpio()
                .toBuilder()
                .possibleValues(null)
                .build();
        Flux<SensorDescription> fux = Flux.just(gpio);
        when(storage.getAll()).thenReturn(fux);

        Long waitFor = gpio.interval();

        // when
        simulator.startGenerateValues();
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(datPublisher).publish(eventArgumentCaptor.capture());
        SensorData gpioCaptured = eventArgumentCaptor.getValue();
        assertEquals(gpio.topic(), gpioCaptured.getTopic());
        assertTrue(CollectionUtils.isEmpty(gpio.possibleValues()));
        assertEquals(
                "Error {There is no possible values for: " + gpio.topic() + "}",
                gpioCaptured.getSensorData()
        );
    }
}
