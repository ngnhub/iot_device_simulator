package com.github.ngnhub.iot_device_simulator.service.simulation.consuming;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getErroredData;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorDataSwitcherTest {

    private ConcurrentHashMap<String, Object> topicToValue;
    private ConcurrentHashMap<String, SensorDescription> topicToDescription;
    @Mock
    private SensorDataPublisher publisher;
    @Captor
    private ArgumentCaptor<SensorData> dataArgumentCaptor;
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

        // when
        var monoData = switcher.switchOn(fan.topic(), 1.0);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.topic().equals(fan.topic()) && data.value().equals("1.0"))
                .verifyComplete();
        assertEquals(1.0, topicToValue.get(fan.topic()));
        verifyErroredDataIsPublished(getSensorData(fan.topic(), "1.0"));
    }

    @Test
    void shouldSendErrorMessageIfTopicDoesNotExist() {
        // given
        assertTrue(topicToValue.isEmpty());
        assertTrue(topicToDescription.isEmpty());
        var expectedMessage = "Error {Topic does not exist or is not switchable}";

        // when
        var monoData = switcher.switchOn("test_topic", 1.0);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() && expectedMessage.equals(data.value()))
                .verifyComplete();
        assertTrue(topicToValue.isEmpty());
        assertTrue(topicToDescription.isEmpty());
        verifyErroredDataIsPublished(getErroredData("test_topic", expectedMessage));
    }

    @Test
    void shouldSendErrorMessageIfInvalidValueType() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);
        var expectedMessage = "Error {Incompatible type: String}";

        // when
        var monoData = switcher.switchOn(fan.topic(), "1.0");

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() && expectedMessage.equals(data.value()))
                .verifyComplete();
        assertEquals(0.0, topicToValue.get(fan.topic()));
        verifyErroredDataIsPublished(getErroredData(fan.topic(), expectedMessage));
    }

    @Test
    void shouldSendErrorMessageIfValueIsNotInPossibleValues() {
        // given
        var fan = fan();
        topicToValue.put(fan.topic(), fan.initValue());
        topicToDescription.put(fan.topic(), fan);
        var expectedMessage =  "Error {\"3.0\" is not possible value for this topic}";

        // when
        var monoData = switcher.switchOn(fan.topic(), 3.0);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() && expectedMessage.equals(data.value()))
                .verifyComplete();
        assertEquals(0.0, topicToValue.get(fan.topic()));
        verifyErroredDataIsPublished(getErroredData(fan.topic(), expectedMessage));
    }

    private void verifyErroredDataIsPublished(SensorData expected) {
        verify(publisher).publish(dataArgumentCaptor.capture());
        var actual = dataArgumentCaptor.getValue();
        assertThat(expected)
                .usingRecursiveComparison()
                .ignoringFields("time")
                .isEqualTo(actual);
    }
}
