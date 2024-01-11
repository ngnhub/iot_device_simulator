package com.github.ngnhub.iot_device_simulator.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@ConditionalOnBean(MqttSensorDataPublisher.class)
@Component
@RequiredArgsConstructor
public class MqttSensorDataPublisherRunner {

    private final MqttSensorDataPublisher publisher;

    @EventListener(ApplicationContextEvent.class)
    public void runMqtt() {
        publisher.subscribeAndPublish()
                .doOnError(err -> scheduleRetry())
                .subscribe();
    }

    public void scheduleRetry() {
        Schedulers.single().schedule(retryTask(), 5L, TimeUnit.SECONDS);
        // TODO: 11.01.2024 get delay from mqtt options
    }

    private Runnable retryTask() {
        return () -> {
            if (publisher.isConnected()) {
                runMqtt();
            } else {
                scheduleRetry();
            }
        };
    }
}
