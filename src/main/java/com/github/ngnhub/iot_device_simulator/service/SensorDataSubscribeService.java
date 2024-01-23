package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import reactor.core.publisher.Flux;

public interface SensorDataSubscribeService {

    Flux<SensorData> subscribeOn(String topic);
}
