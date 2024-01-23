package com.github.ngnhub.iot_device_simulator.mqtt;

import reactor.core.publisher.Flux;

public interface MqttSensorDataProducer {

    Flux<Void> initProduce();
}
