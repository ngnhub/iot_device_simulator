package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.config.InternalProps;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher.SensorDataListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataSubscribeServiceImpl implements SensorDataSubscribeService {

    private static final String LOG_TAG = "[SUBSCRIPTION]";

    private final SensorDataPublisher publisher;
    private final InternalProps internalProps;

    @Override
    public Flux<SensorData> subscribeOn(String topic) {
        return consumeData(topic)
                .retryWhen(getRetrySpec())
                .doOnError(err -> log.error(
                        "{} Subscription failed with error {}", LOG_TAG, err.getMessage()));
    }

    private Flux<SensorData> consumeData(String topic) {
        return Flux.create(sink -> {
            String id = subscribe(topic, new SensorDataListenerImpl(sink));
            sink.onDispose(() -> unsubscribe(topic, id));
        });
    }

    private String subscribe(String topic, SensorDataListener sensorDataListener) {
        var id = publisher.subscribe(topic, sensorDataListener);
        log.debug("{} Subscribed on topic {}. Subscriber id: {}", LOG_TAG, topic, id);
        return id;
    }

    private void unsubscribe(String topic, String id) {
        publisher.unsubscribe(topic, id);
        log.debug("{} Unsubscribed from topic {}. Subscriber id: {}", LOG_TAG, topic, id);
    }

    private RetryBackoffSpec getRetrySpec() {
        var subscriber = internalProps.getSubscriber();
        int retryAttempts = subscriber.getRetryAttempts();
        long delay = subscriber.getRetryDelayMillis();
        return Retry.fixedDelay(retryAttempts, Duration.ofMillis(delay))
                .filter(Exceptions::isOverflow)
                .onRetryExhaustedThrow((ignore1, ignore2) -> Exceptions.failWithOverflow(
                        "Subscription on topic failed after " + retryAttempts
                                + " retries. Consumer processes data too slowly"));
    }

    private record SensorDataListenerImpl(FluxSink<SensorData> dataFluxSink) implements SensorDataListener {

        @Override
        public void onData(SensorData data) {
            dataFluxSink.next(data);
        }

        @Override
        public void onError(SensorData data) {
            log.error("Errored data consumed: {}", data.toString());
        }
    }
}
