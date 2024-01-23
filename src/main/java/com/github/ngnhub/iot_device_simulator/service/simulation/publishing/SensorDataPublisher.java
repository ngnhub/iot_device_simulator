package com.github.ngnhub.iot_device_simulator.service.simulation.publishing;

import com.github.ngnhub.iot_device_simulator.model.SensorData;

public interface SensorDataPublisher {

    void publish(SensorData data);

    String subscribe(String topic, SensorDataListener consumer);

    void unsubscribe(String topic, String id);

    interface SensorDataListener {
        void onData(SensorData data);

        void onError(SensorData data);
    }
}
