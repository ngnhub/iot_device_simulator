package com.github.ngnhub.iot_device_simulator.error;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttWrapperException extends RuntimeException {

    public MqttWrapperException(String message, MqttException cause) {
        super(message, cause);
    }
}
