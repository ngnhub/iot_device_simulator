package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher.SensorDataListener;
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
public class SensorDataSubscribeService {

    private static final String LOG_TAG = "[SUBSCRIPTION]";
    private static final long RETRY_ATTEMPTS = 2L; // TODO: 13.01.2024 to props
    private static final long RETRY_DELAY_MILLIS = 1000L;// TODO: 13.01.2024 to props

    private final SensorDataPublisher publisher;

    public Flux<SensorData> subscribeOn(String topic) {
        return consumeData(topic)
                .retryWhen(getRetrySpec())
                .doOnError(err -> log.error(
                        "{} Subscription failed with error {}", LOG_TAG, err.getMessage()));
    }

    private Flux<SensorData> consumeData(String topic) {
        return Flux.create(sink -> {
            String id = subscribe(topic, new SensorDataListenerImpl(sink));
            sink.onDispose(() -> publisher.unsubscribe(topic, id));
        });
    }

    private String subscribe(String topic, SensorDataListener sensorDataListener) {
        var id = publisher.subscribe(topic, sensorDataListener);
        log.debug("{} Subscribed on topic {}. Subscriber id: {}", LOG_TAG, topic, id);
        return id;
    }

    private static RetryBackoffSpec getRetrySpec() {
        return Retry.fixedDelay(RETRY_ATTEMPTS, Duration.ofMillis(RETRY_DELAY_MILLIS))
                .filter(Exceptions::isOverflow)
                .onRetryExhaustedThrow((ignore1, ignore2) -> Exceptions.failWithOverflow(
                        "Subscription on topic failed after " + RETRY_ATTEMPTS
                                + " retries. Consumer processes data too slowly"));
    }

    private record SensorDataListenerImpl(FluxSink<SensorData> dataFluxSink) implements SensorDataListener {

        @Override
        public void onData(SensorData data) {
            dataFluxSink.next(data);
        }

        @Override
        public void onError(SensorData data) {
            dataFluxSink.next(data);
            dataFluxSink.complete();
        }
    }
}
