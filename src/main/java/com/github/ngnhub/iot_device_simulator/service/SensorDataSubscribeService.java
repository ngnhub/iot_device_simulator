package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataPublisher.SinkKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataSubscribeService {

    private static final String LOG_TAG = "[SUBSCRIPTION]";

    private final SensorDataPublisher publisher;

    public Flux<SensorData> subscribeOn(String topic) {
        return Mono.fromCallable(() -> subscribeAndGetQueue(topic))
                .flatMapMany(sinkKey -> consumeData(topic, sinkKey));
    }

    private SinkKey subscribeAndGetQueue(String topic) {
        SinkKey subscription = publisher.subscribe(topic);
        log.debug("{} Subscribed on topic {}. Subscriber id: {}", LOG_TAG, topic, subscription.subscriberId());
        return subscription;
    }

    private Flux<SensorData> consumeData(String topic, SinkKey sinkKey) {
        return sinkKey.sink().asFlux()
                .doOnError(err -> log.error(
                        "{} Error occurred while consuming data for subscriber id {}",
                        LOG_TAG,
                        sinkKey.subscriberId()
                ))
                .doFinally(s -> publisher.unsubscribe(topic, sinkKey.subscriberId()));
    }
}
