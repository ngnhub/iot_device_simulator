package com.github.ngnhub.iot_device_simulator.service.simulation.consuming;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import reactor.core.publisher.Mono;

public interface SensorDataSwitcher {

    Mono<SensorData> switchOn(String topic, byte[] payload);
}
