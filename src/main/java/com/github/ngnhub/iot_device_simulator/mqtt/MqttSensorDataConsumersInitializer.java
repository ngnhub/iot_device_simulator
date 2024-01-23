package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * mosquitto_pub -h localhost -t gpio1/state -m 1.0 -u admin -P admin
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(MqttClient.class)
public class MqttSensorDataConsumersInitializer {

    private final MqttClient mqttClient;
    private final SensorDataSwitcher switcher;
    private final SensorDescriptionStorage storage;

    public Flux<IMqttMessageListener> initSubscriptionsOnSwitchableTopics() {
        return storage.getOnlySwitchable()
                .flatMap(description -> Mono.fromCallable(() -> subscribe(description)));
    }

    private IMqttMessageListener subscribe(SensorDescription description) throws MqttException {
        IMqttMessageListener listener = (topic, message) -> switcher.switchOn(topic, message.getPayload())
                .subscribe();
        mqttClient.subscribe(description.switcher(), listener);
        return listener;
    }
}
