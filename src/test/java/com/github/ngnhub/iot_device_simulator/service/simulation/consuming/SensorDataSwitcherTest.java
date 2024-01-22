package com.github.ngnhub.iot_device_simulator.service.simulation.consuming;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SensorDataSwitcherTest {

    private ConcurrentHashMap<String, Object> topicToValue;
    private ConcurrentHashMap<String, SensorDescription> topicToDescription;
    private SensorDataSwitcher switcher;

    @BeforeEach
    void setUp() {
        topicToValue = new ConcurrentHashMap<>();
        topicToDescription = new ConcurrentHashMap<>();
        switcher = new SensorDataSwitcher(topicToValue, topicToDescription);
    }

    @Test
    void shouldSwitchValue() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);

        // when
        var monoData = switcher.switchOn(fan.topic(), 1.0);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.topic().equals(fan.topic()) && data.value().equals("1.0"))
                .verifyComplete();
        assertEquals(1.0, topicToValue.get(fan.topic()));
    }

    @Test
    void shouldSendErrorMessageIfTopicDoesNotExist() {
        // given
        assertTrue(topicToValue.isEmpty());
        assertTrue(topicToDescription.isEmpty());

        // when
        var monoData = switcher.switchOn("test_topic", 1.0);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() &&
                        "Error {Topic does not exist or is not switchable}".equals(data.value()))
                .verifyComplete();
        assertTrue(topicToValue.isEmpty());
        assertTrue(topicToDescription.isEmpty());
    }

    @Test
    void shouldSendErrorMessageIfInvalidValueType() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);

        // when
        var monoData = switcher.switchOn(fan.topic(), "1.0");

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() &&
                        "Error {Incompatible type: String}".equals(data.value()))
                .verifyComplete();
        assertEquals(0.0, topicToValue.get(fan.topic()));
    }

    @Test
    void shouldSendErrorMessageIfValueIsNotInPossibleValues() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);

        // when
        var monoData = switcher.switchOn(fan.topic(), 3.0);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() &&
                        "Error {\"3.0\" is not possible value for this topic}".equals(data.value()))
                .verifyComplete();
        assertEquals(0.0, topicToValue.get(fan.topic()));
    }
}
