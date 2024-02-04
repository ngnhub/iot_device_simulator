package com.github.ngnhub.iot_device_simulator.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttProps {

    private boolean enabled;
    private String host;
    private int port;
    private String username;
    private String password;
    private String topicBasePath;
    private boolean enableTopicUniqueIds;
    private Integer qos;
    private Integer reconnectionDelay;
}
