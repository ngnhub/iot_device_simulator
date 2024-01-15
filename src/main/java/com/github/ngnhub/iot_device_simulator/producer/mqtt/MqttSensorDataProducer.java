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

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(MqttClient.class)
public class MqttSensorDataProducer {

    private static final int DEFAULT_QOS = 2; // TODO: 15.01.2024 props

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

    private Mono<Void> publish(SensorData data) {
        Mono<Void> mono = Mono.fromCallable(() -> {
            var topic = generateTopic(data.getTopic());
            var mqttMessage = new MqttMessage();
            mqttMessage.setQos(data.getQos() == null ? DEFAULT_QOS : data.getQos());
            byte[] payload = convertValue(data.getSensorData());
            mqttMessage.setPayload(payload);
            sendMessage(topic, mqttMessage);
            return null;
        });
        return mono.subscribeOn(Schedulers.boundedElastic());
    }

    private void sendMessage(String topic, MqttMessage mqttMessage) throws MqttException {
        mqttClient.publish(topic, mqttMessage);
    }

    private String generateTopic(String sensor) {
        var topicBasePath = props.getTopicBasePath();
        if (props.isEnableTopicUniqueIds()) {
            return generateUniqueTopic(sensor, topicBasePath);
        } else {
            return generateCommonTopic(sensor, topicBasePath);
        }
    }

    private String generateCommonTopic(String sensor, String topicBasePath) {
        if (topicBasePath == null) {
            return sensor;
        }
        return String.format("%s/%s", topicBasePath, sensor);
    }

    private String generateUniqueTopic(String sensor, String topicBasePath) {
        var messageId = UUID.randomUUID();
        if (topicBasePath == null) {
            return String.format("%s/%s", messageId, sensor);
        }
        return String.format("%s/%s/%s", topicBasePath, messageId, sensor);
    }

    private byte[] convertValue(Object value) {
        return value.toString().getBytes();
    }
}
