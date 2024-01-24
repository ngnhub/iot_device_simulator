package com.github.ngnhub.iot_device_simulator.config;

import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = ".*[^/]$", message = "the topic base path should not end with slash")
    private String topicBasePath;
    private boolean enableTopicUniqueIds;
    private Integer qos = 2;
    private Integer reconnectionDelay;
}
