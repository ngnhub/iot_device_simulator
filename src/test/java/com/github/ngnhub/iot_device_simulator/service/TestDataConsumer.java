package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;

public class TestDataConsumer {

    void doOnEvent(SensorData data) {
        System.out.println("Data consumed: " + data);
    }
}
