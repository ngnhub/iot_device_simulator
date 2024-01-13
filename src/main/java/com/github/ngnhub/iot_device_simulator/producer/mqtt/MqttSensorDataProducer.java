package com.github.ngnhub.iot_device_simulator.producer.mqtt;

import com.github.ngnhub.iot_device_simulator.config.MqttProps;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataSubscribeService;
import com.github.ngnhub.iot_device_simulator.service.SensorDescriptionStorage;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.UUID;

//TODO: 09.01.2024 error handling
//TODO: 09.01.2024 test
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(MqttClient.class)
public class MqttSensorDataProducer {

    private static final int DEFAULT_QOS = 2;

    private final SensorDataSubscribeService sensorDataSubscribeService;
    private final SensorDescriptionStorage storage;
    private final MqttClient mqttClient;
    private final MqttProps props;

    @PreDestroy
    public void tearDown() {
        try {
            mqttClient.disconnect();
            log.info("Mqtt client has been disconnected");
        } catch (MqttException e) {
            log.error("Mqtt client disconnection error", e);
        }
    }

    public Flux<Object> subscribeAndProduce() {
        return storage.getAllTopics()
                .flatMap(sensorDataSubscribeService::subscribeOn)
                .flatMap(this::publish);
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    private Mono<Object> publish(SensorData data) {
        return Mono.fromCallable(() -> {
            var topic = generateTopic(data.getTopic());
            var mqttMessage = new MqttMessage();
            mqttMessage.setQos(data.getQos() == null ? DEFAULT_QOS : data.getQos());
            byte[] payload = convertValue(data.getSensorData());
            mqttMessage.setPayload(payload);
            sendMessage(topic, mqttMessage);
            return null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void sendMessage(String topic, MqttMessage mqttMessage) throws MqttException {
        mqttClient.publish(topic, mqttMessage);
    }

    private String generateTopic(String sensor) {
        var messageId = UUID.randomUUID(); // TODO: 10.01.2024 switcher, it may be unnecassery
        var topicBasePath = props.getTopicBasePath();
        if (topicBasePath == null) {
            return String.format("%s/%s", messageId, sensor);
        }
        return String.format("%s/%s/%s", topicBasePath, messageId, sensor);
    }

    private byte[] convertValue(Object value) {
        return value.toString().getBytes();
    }
}