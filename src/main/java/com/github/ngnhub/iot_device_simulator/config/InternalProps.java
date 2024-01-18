package com.github.ngnhub.iot_device_simulator.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "internal")
public class InternalProps {

    private Subscriber subscriber;

    @Data
    public static class Subscriber {
        private int retryAttempts;
        private long retryDelayMillis;
    }
}
