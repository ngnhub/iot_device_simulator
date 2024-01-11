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
public class SensorDataChannel {

    private final SensorDataSubscribeService buffer;

    public Flux<SensorData> subscribe(String topic) {
        return Mono.fromCallable(() -> buffer.subscribe(topic))
                .flatMapMany(sinkKey -> sinkKey.sink().asFlux()
                        .doFinally(s -> buffer.unsubscribe(topic, sinkKey.id())));
    }
}
