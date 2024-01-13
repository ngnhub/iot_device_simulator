package com.github.ngnhub.iot_device_simulator.producer.rsocket;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataSubscribeService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RSocketSensorDataProducer {

    private final SensorDataSubscribeService publisher;

    @MessageMapping("subscribe/{topic}")
    public Flux<SensorData> stream(@DestinationVariable String topic) {
        return publisher.subscribeOn(topic);
    }

    @MessageMapping("subscribe/value/{topic}")
    public Flux<String> streamValues(@DestinationVariable String topic) {
        return publisher.subscribeOn(topic)
                .map(SensorData::getSensorData);
    }
}
