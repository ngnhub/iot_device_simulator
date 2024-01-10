package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.event.SensorValueUpdatedEvent;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Sinks;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: 10.01.2024 error handling
// TODO: 10.01.2024 limits
// TODO: 10.01.2024 tests
@Component
@RequiredArgsConstructor
public class SensorDataSubscribeService {

    private final ConcurrentHashMap<String,
            Map<String, Sinks.Many<SensorData<?>>>> TOPICS_TO_MESSAGE_QUEUES = new ConcurrentHashMap<>();

    @EventListener
    public void onEvent(SensorValueUpdatedEvent event) {
        var data = event.data();
        var topic = data.getTopic();
        TOPICS_TO_MESSAGE_QUEUES.compute(topic, (key, queues) -> fanOut(data, queues));
    }

    private Map<String, Sinks.Many<SensorData<?>>> fanOut(SensorData<?> data,
                                                          Map<String, Sinks.Many<SensorData<?>>> queues) {
        if (queues == null) {
            queues = new HashMap<>(100);
        } else {
            queues.values().forEach(sink -> sink.tryEmitNext(data));
        }
        return queues;
    }

    public SinkKey subscribe(String topic) {
        var id = UUID.randomUUID().toString();
        var sink = addNewQueue(topic, id);
        return new SinkKey(id, sink);
    }

    private Sinks.Many<SensorData<?>> addNewQueue(String topic, String id) {
        Sinks.Many<SensorData<?>> sink = Sinks.many().unicast().onBackpressureBuffer();
        TOPICS_TO_MESSAGE_QUEUES.compute(topic, (key, queues) -> {
            if (queues == null) {
                queues = new HashMap<>();
            }
            queues.put(id, sink);
            return queues;
        });
        return sink;
    }

    public void unsubscribe(String topic, String id) {
        TOPICS_TO_MESSAGE_QUEUES.computeIfPresent(topic, (key, val) -> {
            val.remove(id);
            return val;
        });
    }

    public record SinkKey(String id, Sinks.Many<SensorData<?>> sink) {}
}
