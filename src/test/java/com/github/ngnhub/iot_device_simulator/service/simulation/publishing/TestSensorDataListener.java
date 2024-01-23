package com.github.ngnhub.iot_device_simulator.service.simulation.publishing;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher.SensorDataListener;

public class TestSensorDataListener implements SensorDataListener {

    @Override
    public void onData(SensorData data) {
        System.out.println("Received: " + data);
    }

    @Override
    public void onError(SensorData data) {
        System.out.println("Received errored: " + data);
    }
}
