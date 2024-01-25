package com.github.ngnhub.iot_device_simulator.service.simulation.consuming;

import com.github.ngnhub.iot_device_simulator.error.NotFoundException;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getErroredData;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorDataSwitcherImplTest {

    private ConcurrentHashMap<String, SensorDescription> topicToDescription;
    @Mock
    private SensorDataPublisherImpl publisher;
    @Captor
    private ArgumentCaptor<SensorData> dataArgumentCaptor;
    private SensorDataSwitcher switcher;

    @BeforeEach
    void setUp() {
        topicToDescription = new ConcurrentHashMap<>();
        switcher = new SensorDataSwitcherImpl(topicToDescription, publisher);
    }

    @Test
    void shouldSwitchValue() {
        // given
        var fan = fan();
        topicToDescription.put(Objects.requireNonNull(fan.switcher()), fan);
        byte[] bytes = String.valueOf(1.0).getBytes();

        // when
        var monoData = switcher.switchOn(fan.switcher(), bytes);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.topic().equals(fan.topic()) && data.value().equals("1.0"))
                .verifyComplete();
        verifyErroredDataIsPublished(getSensorData(fan.topic(), "1.0"));
    }

    @Test
    void shouldSendErrorMessageIfTopicDoesNotExist() {
        // given
        assertTrue(topicToDescription.isEmpty());
        byte[] bytes = String.valueOf(1.0).getBytes();

        // when
        var monoData = switcher.switchOn("test_topic", bytes);

        // then
        StepVerifier.create(monoData)
                .verifyErrorMatches(err -> err instanceof NotFoundException
                        && "Topic does not exist or is not switchable: test_topic".equals(err.getMessage()));
        assertTrue(topicToDescription.isEmpty());
        verify(publisher, never()).publish(any());
    }

    @Test
    void shouldSendErrorMessageIfInvalidValueType() {
        // given
        var fan = fan();
        topicToDescription.put(Objects.requireNonNull(fan.switcher()), fan);
        var expectedMessage = "Error occurred during the data processing " +
                "{ Can not convert the consumed value. Should have type: Double }";
        byte[] bytes = "value".getBytes();

        // when
        var monoData = switcher.switchOn(fan.switcher(), bytes);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() && expectedMessage.equals(data.value()))
                .verifyComplete();
        verifyErroredDataIsPublished(getErroredData(fan.topic(), expectedMessage));
    }

    @Test
    void shouldSendErrorMessageIfValueIsNotInPossibleValues() {
        // given
        var fan = fan();
        topicToDescription.put(Objects.requireNonNull(fan.switcher()), fan);
        var expectedMessage = "Error occurred during the data processing { \"3.0\" is not possible value for this topic }";
        byte[] bytes = String.valueOf(3.0).getBytes();

        // when
        var monoData = switcher.switchOn(fan.switcher(), bytes);

        // then
        StepVerifier.create(monoData)
                .expectNextMatches(data -> data.errored() && expectedMessage.equals(data.value()))
                .verifyComplete();
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
