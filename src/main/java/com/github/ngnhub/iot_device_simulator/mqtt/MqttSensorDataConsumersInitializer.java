package com.github.ngnhub.iot_device_simulator.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import reactor.core.publisher.Flux;

public interface MqttSensorDataConsumersInitializer {

    Flux<IMqttMessageListener> initSubscriptionsOnSwitchableTopics();
}
