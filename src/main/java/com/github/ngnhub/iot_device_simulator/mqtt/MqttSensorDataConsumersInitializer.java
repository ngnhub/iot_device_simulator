package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(MqttClient.class)
public class MqttSensorDataConsumersInitializer {

    private final MqttClient mqttClient;
    private final SensorDataSwitcher switcher;
    private final Flux<String> topicsOfSwitchableDevices;
    private final SensorDataPublisher publisher;

    // TODO: 22.01.2024 test
    public Flux<Void> initSubscriptionsOnSwitchableTopics() {
        return topicsOfSwitchableDevices
                .flatMap(topic -> Mono.fromCallable(() -> {
                    subscribe(topic);
                    return null;
                }));
    }

    private void subscribe(String switchableTopic) throws MqttException {
        mqttClient.subscribe(
                switchableTopic,
                (topic, message) -> switcher
                        .switchOn(topic, convert(message))
                        .subscribe(publisher::publish)
        );
    }

    private String convert(MqttMessage message) {
        byte[] payload = message.getPayload();
        return new String(payload, StandardCharsets.UTF_8);
    }
}
