package com.github.ngnhub.iot_device_simulator.producer;

import com.github.ngnhub.iot_device_simulator.model.ChangeDeviceValueRequest;
import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttSensorDataConsumer {

    private final MqttClient client;
    private final SensorDataSwitcher switcher;
    private final List<String> topicsOfSwitchableDevices;

    public void initSubscribe() throws MqttException {
        for (String topicsOfSwitchableDevice : topicsOfSwitchableDevices) {
            getSubscribe(topicsOfSwitchableDevice);
        }
    }

    private void getSubscribe(String switchableTopic) throws MqttException {
        client.subscribe(
                switchableTopic,
                (topic, message) -> switcher
                        .switchOn(Mono.just(new ChangeDeviceValueRequest(topic, message)))
                        .subscribe()
        );
    }
}
