package com.github.ngnhub.iot_device_simulator.publisher;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataChannel;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RSocketSensorDataPublisher {

    private final SensorDataChannel publisher;

    @MessageMapping("subscribe/{topic}")
    public Flux<SensorData<?>> stream(@DestinationVariable String topic) {
        return publisher.subscribe(topic);
    }
}
