package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

// TODO: 10.01.2024 test
// TODO: 10.01.2024 error handling
@Service
@RequiredArgsConstructor
public class SensorDataSubscribeService {

    private final SensorDataPublisher publisher;

    public Flux<SensorData> subscribe(String topic) {
        return Mono.fromCallable(() -> publisher.subscribe(topic))
                .flatMapMany(sinkKey -> sinkKey.sink().asFlux()
                        .doFinally(s -> publisher.unsubscribe(topic, sinkKey.id())));
    }
}
