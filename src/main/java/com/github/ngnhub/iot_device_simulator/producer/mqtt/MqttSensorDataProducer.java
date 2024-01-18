package com.github.ngnhub.iot_device_simulator.producer.mqtt;

import com.github.ngnhub.iot_device_simulator.config.MqttProps;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataSubscribeService;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import jakarta.annotation.Nullable;
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

import static com.github.ngnhub.iot_device_simulator.config.MqttClientConfig.MQTT_LOG_TAG;

/**
 * to read locally mosquitto_sub -h localhost -p 1883 -t +/gpio -u admin -P admin
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(MqttClient.class)
public class MqttSensorDataProducer {

    private final SensorDataSubscribeService sensorDataSubscribeService;
    private final SensorDescriptionStorage storage;
    private final MqttClient mqttClient;
    private final MqttProps props;

    @PreDestroy
    public void tearDown() {
        try {
            mqttClient.disconnect();
            log.info("{} Mqtt client has been disconnected", MQTT_LOG_TAG);
        } catch (MqttException e) {
            log.error("{} Mqtt client disconnection error", MQTT_LOG_TAG, e);
        }
    }

    public Flux<Void> subscribeAndProduce() {
        return storage.getAll()
                .flatMap(description -> sensorDataSubscribeService
                        .subscribeOn(description.topic())
                        .flatMap(data -> publish(data, description.qos())));
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    private Mono<Void> publish(SensorData data, @Nullable Integer qos) {
        Mono<Void> mono = Mono.fromCallable(() -> {
            var topic = generateTopic(data.getTopic());
            var mqttMessage = new MqttMessage();
            mqttMessage.setQos(qos == null ? props.getQos() : qos);
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
