package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.error.SinkOverflowException;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataPublisher.SinkKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        return Mono.fromCallable(() -> subscribeAndGetQueue(topic))
                .flatMapMany(sinkKey -> sinkKey.sink().asFlux()
                        .doFinally(s -> publisher.unsubscribe(topic, sinkKey.subscriberId())));
    }

    private SinkKey subscribeAndGetQueue(String topic) {
        SinkKey subscription = publisher.subscribe(topic);
        log.debug("{} Subscribed on topic {}. Subscriber id: {}", LOG_TAG, topic, subscription.subscriberId());
        return subscription;
    }

    private static RetryBackoffSpec getRetrySpec() {
        return Retry.fixedDelay(RETRY_ATTEMPTS, Duration.ofMillis(RETRY_DELAY_MILLIS))
                .filter(err -> err instanceof SinkOverflowException)
                .onRetryExhaustedThrow((ignore1, ignore2) -> new SinkOverflowException(
                        "Subscription on topic failed after {} retries. Consumer processes data too slowly"));
    }
}
