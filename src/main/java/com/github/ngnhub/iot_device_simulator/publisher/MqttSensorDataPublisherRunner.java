package com.github.ngnhub.iot_device_simulator.publisher;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@ConditionalOnBean(MqttSensorDataPublisher.class)
@Component
@RequiredArgsConstructor
public class MqttSensorDataPublisherRunner {

    private final MqttSensorDataPublisher publisher;

    @EventListener(ApplicationContextEvent.class)
    public void runMqtt() {
        publisher.startPublishing();
    }
}
