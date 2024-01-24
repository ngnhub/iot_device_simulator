package com.github.ngnhub.iot_device_simulator.config;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mqtt.enabled", matchIfMissing = true)
public class MqttClientConfig {

    public static String MQTT_LOG_TAG = "[MQTT]";

    @Bean
    public MqttClient mqttClient(MqttProps props, MqttConnectOptions options) throws MqttException {
        var publisherId = UUID.randomUUID().toString();
        var url = "tcp://" + props.getHost() + ":" + props.getPort();
        MqttClient mqttClient = new MqttClient(url, publisherId, new MqttDefaultFilePersistence("/tmp"));
        mqttClient.connect(options);
        return mqttClient;
    }

    @Bean
    public MqttConnectOptions mqttOptions(MqttProps props) {
        var options = new MqttConnectOptions();
        var username = props.getUsername();
        if (!ObjectUtils.isEmpty(username)) {
            options.setUserName(props.getUsername());
        }
        var password = props.getPassword();
        if (!ObjectUtils.isEmpty(password)) {
            options.setPassword(password.toCharArray());
        }
        var reconnectionDelay = props.getReconnectionDelay();
        if (!ObjectUtils.isEmpty(reconnectionDelay)) {
            options.setMaxReconnectDelay(reconnectionDelay);
        }
        options.setAutomaticReconnect(true);
        return options;
    }
}
