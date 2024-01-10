package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.event.SensorValueUpdatedEvent;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.scheduler.VirtualTimeScheduler;

import org.springframework.context.ApplicationEventPublisher;
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


@ExtendWith(MockitoExtension.class)
class SensorDataSimulatorTest {

    @Mock
    private SensorDescriptionStorage storage;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Captor
    private ArgumentCaptor<SensorValueUpdatedEvent> eventArgumentCaptor;
    private SensorDataSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new SensorDataSimulator(storage, eventPublisher);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenZeroAndFive() {
        // given
        SensorDescription gpio = gpio();
        SensorDescription temperature = temperature();
        Flux<SensorDescription> fux = Flux.just(gpio, temperature);
        when(storage.getAll()).thenReturn(fux);
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        assertTrue(temperature.interval() > gpio.interval());
        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        scheduler.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(eventPublisher, times(2)).publishEvent(eventArgumentCaptor.capture());
        List<SensorValueUpdatedEvent> allValues = eventArgumentCaptor.getAllValues();
        assertEquals(2, allValues.size());

        SensorData<?> gpioCaptured = allValues.get(0).data();
        assertEquals(gpio.topic(), gpioCaptured.getTopic());
        assertTrue(gpioCaptured.getSensorData() instanceof String);
        assertFalse(CollectionUtils.isEmpty(gpio.possibleValues()));
        assertTrue(gpio.possibleValues().contains(gpioCaptured.getSensorData()));

        SensorData<?> temperatureCaptured = allValues.get(1).data();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());
        assertTrue(temperatureCaptured.getSensorData() instanceof Double);
        Double sensorData = (Double) temperatureCaptured.getSensorData();
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
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        scheduler.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        SensorData<?> temperatureCaptured = eventArgumentCaptor.getValue().data();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());
        assertTrue(temperatureCaptured.getSensorData() instanceof Double);
        Double sensorData = (Double) temperatureCaptured.getSensorData();
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
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        scheduler.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        SensorData<?> temperatureCaptured = eventArgumentCaptor.getValue().data();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());
        assertTrue(temperatureCaptured.getSensorData() instanceof Double);
        Double sensorData = (Double) temperatureCaptured.getSensorData();
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
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        Long waitFor = temperature.interval();

        // when
        simulator.startGenerateValues();
        scheduler.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        SensorData<?> temperatureCaptured = eventArgumentCaptor.getValue().data();
        assertEquals(temperature.topic(), temperatureCaptured.getTopic());
        assertTrue(temperatureCaptured.getSensorData() instanceof Double);
        double sensorData = (double) temperatureCaptured.getSensorData();
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
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        Long waitFor = gpio.interval();

        // when
        simulator.startGenerateValues();
        scheduler.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        SensorData<?> gpioCaptured = eventArgumentCaptor.getValue().data();
        assertEquals(gpio.topic(), gpioCaptured.getTopic());
        assertTrue(gpioCaptured.getSensorData() instanceof String);
        assertTrue(CollectionUtils.isEmpty(gpio.possibleValues()));
        assertEquals(
                "Error {There is no possible values for: " + gpio.topic() + "}",
                gpioCaptured.getSensorData()
        );
    }

    @Test
    void shouldPGenerateErrorMessageWrongSensorValueType() {
        // given
        SensorDescription gpio = gpio()
                .toBuilder()
                .type("Float")
                .build();
        Flux<SensorDescription> fux = Flux.just(gpio);
        when(storage.getAll()).thenReturn(fux);
        VirtualTimeScheduler scheduler = VirtualTimeScheduler.getOrSet();
        Long waitFor = gpio.interval();

        // when
        simulator.startGenerateValues();
        scheduler.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        SensorData<?> gpioCaptured = eventArgumentCaptor.getValue().data();
        assertEquals(gpio.topic(), gpioCaptured.getTopic());
        assertEquals(
                "Error {Unsupported type: " + gpio.type() + "}",
                gpioCaptured.getSensorData()
        );
    }
}
