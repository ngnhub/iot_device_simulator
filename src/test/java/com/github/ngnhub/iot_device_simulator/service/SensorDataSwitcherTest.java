package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.ChangeDeviceValueRequest;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataSwitcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SensorDataSwitcherTest {

    private ConcurrentHashMap<String, Object> topicToValue;
    private ConcurrentHashMap<String, SensorDescription> topicToDescription;
    @Mock
    private SensorDataPublisher publisher;
    private SensorDataSwitcher switcher;

    @BeforeEach
    void setUp() {
        topicToValue = new ConcurrentHashMap<>();
        topicToDescription = new ConcurrentHashMap<>();
        switcher = new SensorDataSwitcher(topicToValue, topicToDescription, publisher);
    }

    @Test
    void shouldSwitchValue() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);
        var request = Mono.just(new ChangeDeviceValueRequest(fan.topic(), 1.0));

        // when
        var monoData = switcher.switchOn(request);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.getTopic().equals(fan.topic()) && data.getSensorData().equals("1.0"))
                .verifyComplete();
        assertEquals(1.0, topicToValue.get(fan.topic()));
    }

    @Test
    void shouldSendErrorMessageIfTopicDoesNotExist() {
        // given
        var request = Mono.just(new ChangeDeviceValueRequest("test_topic", 1.0));
        assertTrue(topicToValue.isEmpty());
        assertTrue(topicToDescription.isEmpty());

        // when
        var monoData = switcher.switchOn(request);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.isErrored() &&
                        "Error {Topic does not exist or is not switchable}".equals(data.getSensorData()))
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
        var request = Mono.just(new ChangeDeviceValueRequest(fan.topic(), "1.0"));

        // when
        var monoData = switcher.switchOn(request);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.isErrored() &&
                        "Error {Incompatible type: String}".equals(data.getSensorData()))
                .verifyComplete();
        assertEquals(0.0, topicToValue.get(fan.topic()));
    }

    @Test
    void shouldSendErrorMessageIfValueIsNotInPossibleValues() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);
        var request = Mono.just(new ChangeDeviceValueRequest(fan.topic(), 3.0));

        // when
        var monoData = switcher.switchOn(request);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.isErrored() &&
                        "Error {\"3.0\" is not possible value for this topic}".equals(data.getSensorData()))
                .verifyComplete();
        assertEquals(0.0, topicToValue.get(fan.topic()));
    }
}
