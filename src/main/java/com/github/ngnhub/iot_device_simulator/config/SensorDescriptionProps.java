package com.github.ngnhub.iot_device_simulator.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "description")
public class SensorDescriptionProps {

    private String defaultResource;
    private String path;
}
