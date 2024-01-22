package com.github.ngnhub.iot_device_simulator.producer.mqtt;

import com.github.ngnhub.iot_device_simulator.error.MqttWrapperException;
import com.github.ngnhub.iot_device_simulator.producer.MqttSensorDataConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import reactor.core.scheduler.Scheduler;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.ngnhub.iot_device_simulator.config.MqttClientConfig.MQTT_LOG_TAG;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_CLOSED;
import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CONNECTION_LOST;

@Slf4j
@ConditionalOnBean(MqttSensorDataProducer.class)
@Component
@RequiredArgsConstructor
public class MqttSensorDataProducerRunner {

    private static final Set<Integer> RETRIABLE_REASONS = Set.of(
            Short.valueOf(REASON_CODE_CONNECTION_LOST).intValue(),
            Short.valueOf(REASON_CODE_CLIENT_CLOSED).intValue()
    );

    private final MqttSensorDataProducer producer;
    private final MqttSensorDataConsumer consumer;
    private final MqttConnectOptions options;

    @Lookup
    public Scheduler singleThreadScheduler() {
        return null;
    }

    @EventListener(ApplicationContextEvent.class)
    public void runMqtt() {
        producer.subscribeAndProduce()
                .doOnError(this::handleError)
                .onErrorComplete()
                .subscribe();
        try {
            consumer.initSubscribe();
        } catch (MqttException e) {
            throw new MqttWrapperException("Exception occurred during subscribing", e);
        }
    }

    private void handleError(Throwable err) {
        log.error("{} Error occurred while publishing mqtt messages: {}", MQTT_LOG_TAG, err.getMessage());
        if (err instanceof MqttException && RETRIABLE_REASONS.contains(((MqttException) err).getReasonCode())) {
            log.info("{} Try to revive mqtt connection...", MQTT_LOG_TAG);
            scheduleRetry();
        }
    }

    /**
     * Tries to reconnect basing on {@link MqttConnectOptions#getMaxReconnectDelay()}.
     * It divides the maximum reconnection delay by 2 to minimize inaccuracy when the retry is scheduled just before
     * the reconnection.
     */
    private void scheduleRetry() {
        int reconnectionDelay = options.getMaxReconnectDelay() / 2;
        singleThreadScheduler().schedule(retryTask(), reconnectionDelay, TimeUnit.MILLISECONDS);
    }

    private Runnable retryTask() {
        return () -> {
            if (producer.isConnected()) {
                runMqtt();
                log.info("{} Mqtt publishing revived", MQTT_LOG_TAG);
            } else {
                scheduleRetry();
                log.info("{} Mqtt client is still disconnected...", MQTT_LOG_TAG);
            }
        };
    }
}
