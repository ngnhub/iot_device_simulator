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
public class MqttClientConfig {

    @Bean
    @ConditionalOnProperty(name = "mqtt.enabled", matchIfMissing = true)
    public MqttClient mqttClient(MqttProps props) throws MqttException {
        var publisherId = UUID.randomUUID().toString();
        var url = "tcp://" + props.getHost() + ":" + props.getPort();
        var mqttClient = new MqttClient(url, publisherId, new MqttDefaultFilePersistence("/tmp"));
        var options = new MqttConnectOptions();
        options.setUserName(props.getUsername());
        String password = props.getPassword();
        if (password != null) {
            options.setPassword(password.toCharArray());
        }
        options.setAutomaticReconnect(true);
        mqttClient.connect(options);
        return mqttClient;
    }
}
