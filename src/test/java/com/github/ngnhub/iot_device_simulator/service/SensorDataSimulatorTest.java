package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SensorDataSimulatorTest {

    @Mock
    private SensorDescriptionStorage storage;
    private SensorDataSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new SensorDataSimulator(storage);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenZeroAndFive() {
        // given
        var sensorName = "sensor";
        var description = new SensorDescription(sensorName, "double", 5.0, 0.0, null, 100L);
        when(storage.getBy(sensorName)).thenReturn(Mono.just(description));

        // when
        Flux<?> result1 = simulator.generate(sensorName).take(3L);

        // then
        StepVerifier.withVirtualTime(() -> result1)
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, 0.0, 5.0))
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, 0.0, 5.0))
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, 0.0, 5.0))
                .verifyComplete();
        verify(storage).getBy(sensorName);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenMinDoubleAndFive() {
        // given
        var sensorName = "sensor";
        var description = new SensorDescription(sensorName, "double", 5.0, null, null, 100L);
        when(storage.getBy(sensorName)).thenReturn(Mono.just(description));

        // when
        Flux<?> result1 = simulator.generate(sensorName).take(3L);

        // then
        StepVerifier.withVirtualTime(() -> result1)
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, Double.MIN_VALUE, 5.0))
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, Double.MIN_VALUE, 5.0))
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, Double.MIN_VALUE, 5.0))
                .verifyComplete();
        verify(storage).getBy(sensorName);
    }

    @Test
    void shouldGenerateDoubleValuesBetweenZeroAndMazDouble() {
        // given
        var sensorName = "sensor";
        var description = new SensorDescription(sensorName, "double", 5.0, null, null, 100L);
        when(storage.getBy(sensorName)).thenReturn(Mono.just(description));

        // when
        Flux<?> result1 = simulator.generate(sensorName).take(3L);

        // then
        StepVerifier.withVirtualTime(() -> result1)
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, 0.0, Double.MAX_VALUE))
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, 0.0, Double.MAX_VALUE))
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(val -> isDoubleAndInRange(val, 0.0, Double.MAX_VALUE))
                .verifyComplete();
        verify(storage).getBy(sensorName);
    }

    private boolean isDoubleAndInRange(Object val, Double min, Double max) {
        return val instanceof Double && min <= (Double) val && (Double) val <= max;
    }

    @Test
    void shouldGeneratePossibleStringValues() {
        // given
        var sensorName = "sensor";
        List<String> possibleStringValues = List.of("on", "off", "paused");
        var description = new SensorDescription(
                sensorName,
                "string",
                null,
                null,
                possibleStringValues,
                100L
        );
        when(storage.getBy(sensorName)).thenReturn(Mono.just(description));

        // when
        Flux<?> result1 = simulator.generate(sensorName).take(3L);

        // then
        StepVerifier.withVirtualTime(() -> result1)
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(100L))
                .expectNextMatches(possibleStringValues::contains)
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(possibleStringValues::contains)
                .thenAwait(Duration.ofMillis(100L))
                .expectNextMatches(possibleStringValues::contains)
                .verifyComplete();
        verify(storage).getBy(sensorName);
    }

    @Test
    void shouldThrowWhenNoPossibleValuesPresent() {
        // given
        var sensorName = "sensor";
        var description = new SensorDescription(
                sensorName,
                "string",
                null,
                null,
                null,
                100L
        );
        when(storage.getBy(sensorName)).thenReturn(Mono.just(description));

        // when
        Flux<?> generated = simulator.generate(sensorName);

        // then
        StepVerifier.create(generated)
                .expectErrorMatches(err -> err instanceof IllegalArgumentException
                        && err.getMessage().equals("There is no possible values for: " + sensorName))
                .verify();
        verify(storage).getBy(sensorName);
    }

    @Test
    void shouldThrowWhenWrongSensorValueType() {
        // given
        var sensorName = "sensor";
        var wrongType = "integer";
        var description = new SensorDescription(
                sensorName,
                wrongType,
                null,
                null,
                null,
                100L
        );
        when(storage.getBy(sensorName)).thenReturn(Mono.just(description));

        // when
        Flux<?> generated = simulator.generate(sensorName);

        // then
        StepVerifier.create(generated)
                .expectErrorMatches(err -> err instanceof UnsupportedOperationException
                        && err.getMessage().equals("Unexpected type: " + wrongType))
                .verify();
        verify(storage).getBy(sensorName);
    }
}
