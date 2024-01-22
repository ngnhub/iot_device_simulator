package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import com.github.ngnhub.iot_device_simulator.utils.SensorValueType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * mosquitto_pub -h localhost -t gpio1 -m "1.0" -u admin -P admin
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(MqttClient.class)
public class MqttSensorDataConsumersInitializer {

    private final MqttClient mqttClient;
    private final SensorDataSwitcher switcher;
    private final SensorDescriptionStorage storage;

    public Flux<Void> initSubscriptionsOnSwitchableTopics() {
        return storage.getOnlySwitchable()
                .flatMap(description -> Mono.fromCallable(() -> {
                    subscribe(description);
                    return null;
                }));
    }

    private void subscribe(SensorDescription description) throws MqttException {
        mqttClient.subscribe(
                description.switcher(),
                (topic, message) -> convert(message, description.type())
                        .flatMap(val -> switcher.switchOn(topic, val))
                        .subscribe()
        );
    }

    private Mono<Object> convert(MqttMessage message, SensorValueType type) {
        return Mono.defer(() -> {
            byte[] payload = message.getPayload();
            return type.getFromByteConverter()
                    .apply(payload)
                    .map(Mono::just)
                    .orElseThrow(() -> new IllegalArgumentException("Can not convert consumed value"));
        });
    }
}
