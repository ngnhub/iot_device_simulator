package com.github.ngnhub.iot_device_simulator.config;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    // TODO: 15.01.2024 reconnection delay to props
    @Bean
    public MqttConnectOptions mqttOptions(MqttProps props) {
        var options = new MqttConnectOptions();
        options.setUserName(props.getUsername());
        var password = props.getPassword();
        if (password != null) {
            options.setPassword(password.toCharArray());
        }
        options.setAutomaticReconnect(true);
        return options;
    }
}
