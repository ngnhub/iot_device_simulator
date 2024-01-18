package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.github.ngnhub.iot_device_simulator.model.ChangeDeviceValueRequest;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import reactor.core.publisher.Mono;

public interface SensorDataSwitcher {

    Mono<SensorData> switchOn(Mono<ChangeDeviceValueRequest> request);
}
