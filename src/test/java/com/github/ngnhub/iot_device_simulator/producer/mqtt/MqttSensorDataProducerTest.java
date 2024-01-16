package com.github.ngnhub.iot_device_simulator.producer.mqtt;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.config.MqttProps;
import com.github.ngnhub.iot_device_simulator.service.SensorDataSubscribeService;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.temperature;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MqttSensorDataProducerTest extends BaseTest {

    private static final String UUID_PATTERN = "([0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12})";

    @Mock
    private SensorDataSubscribeService sensorDataSubscribeService;
    @Mock
    private SensorDescriptionStorage storage;
    @Mock
    private MqttClient mqttClient;
    @Captor
    private ArgumentCaptor<MqttMessage> mqttMessageArgumentCaptor;
    private MqttProps props;
    private MqttSensorDataProducer producer;

    @BeforeEach
    void setUp() {
        props = new MqttProps();
        producer = new MqttSensorDataProducer(sensorDataSubscribeService, storage, mqttClient, props);
    }

    @Test
    void shouldDisconnect() throws Exception {
        // when
        producer.tearDown();

        // then
        verify(mqttClient).disconnect();
    }

    @Test
    void shouldSendTwoMessages() throws Exception {
        // given
        props.setEnableTopicUniqueIds(false);
        var gpioTopic = gpio().topic();
        var temperatureTopic = temperature().topic();
        when(storage.getAllTopics()).thenReturn(Flux.just(gpioTopic, temperatureTopic));
        var gpioData = getSensorData(gpioTopic, "1");
        var temperatureData = getSensorData(temperatureTopic, "15");
        gpioData.setQos(null);
        temperatureData.setQos(1);
        when(sensorDataSubscribeService.subscribeOn(gpioTopic)).thenReturn(Flux.just(gpioData));
        when(sensorDataSubscribeService.subscribeOn(temperatureTopic)).thenReturn(Flux.just(temperatureData));

        // when
        Flux<Void> flux = producer.subscribeAndProduce().take(2);

        // then
        StepVerifier.create(flux).verifyComplete();

        verify(mqttClient).publish(eq(gpioTopic), mqttMessageArgumentCaptor.capture());
        MqttMessage message = mqttMessageArgumentCaptor.getValue();
        assertEquals(2, message.getQos());
        assertEquals(gpioData.getSensorData(), new String(message.getPayload()));

        verify(mqttClient).publish(eq(temperatureTopic), mqttMessageArgumentCaptor.capture());
        message = mqttMessageArgumentCaptor.getValue();
        assertEquals(1, message.getQos());
        assertEquals(temperatureData.getSensorData(), new String(message.getPayload()));
    }

    @Test
    void shouldSendMessageWithBasePath() throws Exception {
        // given
        props.setEnableTopicUniqueIds(false);
        var basePath = "/base/path";
        props.setTopicBasePath(basePath);
        var gpioTopic = gpio().topic();

        when(storage.getAllTopics()).thenReturn(Flux.just(gpioTopic));
        var gpioData = getSensorData(gpioTopic, "1");
        when(sensorDataSubscribeService.subscribeOn(gpioTopic)).thenReturn(Flux.just(gpioData));

        // when
        Flux<Void> flux = producer.subscribeAndProduce().take(1);

        // then
        StepVerifier.create(flux).verifyComplete();
        verify(mqttClient).publish(eq(basePath + "/" + gpioTopic), any());
    }

    @Test
    void shouldSendMessageWithId() throws Exception {
        // given
        props.setEnableTopicUniqueIds(true);
        var gpioTopic = gpio().topic();

        when(storage.getAllTopics()).thenReturn(Flux.just(gpioTopic));
        var gpioData = getSensorData(gpioTopic, "1");
        when(sensorDataSubscribeService.subscribeOn(gpioTopic)).thenReturn(Flux.just(gpioData));

        // when
        Flux<Void> flux = producer.subscribeAndProduce().take(1);

        // then
        StepVerifier.create(flux).verifyComplete();

        verify(mqttClient).publish(matches("^" + UUID_PATTERN + "/gpio$"), any());
    }

    @Test
    void shouldSendMessageWithIdAndBasePath() throws Exception {
        // given
        props.setEnableTopicUniqueIds(true);
        var basePath = "/base/path";
        props.setTopicBasePath(basePath);
        var gpioTopic = gpio().topic();

        when(storage.getAllTopics()).thenReturn(Flux.just(gpioTopic));
        var gpioData = getSensorData(gpioTopic, "1");
        when(sensorDataSubscribeService.subscribeOn(gpioTopic)).thenReturn(Flux.just(gpioData));

        // when
        Flux<Void> flux = producer.subscribeAndProduce().take(1);

        // then
        StepVerifier.create(flux).verifyComplete();

        verify(mqttClient).publish(matches("^" + basePath + "/" + UUID_PATTERN + "/gpio$"), any());
    }

    @Test
    void shouldBeConnected() {
        // given
        when(mqttClient.isConnected()).thenReturn(true);

        // when
        boolean connected = producer.isConnected();

        // then
        assertTrue(connected);
    }
}
