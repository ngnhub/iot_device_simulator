package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Sinks;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SensorDataPublisher {

    private final ConcurrentHashMap<String, Map<String, Sinks.Many<SensorData>>> topicToMessageQueues;

    public void publish(SensorData data) {
        var topic = data.getTopic();
        topicToMessageQueues.computeIfPresent(topic, (key, queues) -> fanOut(data, queues));
    }

    private Map<String, Sinks.Many<SensorData>> fanOut(SensorData data, Map<String, Sinks.Many<SensorData>> queues) {
        queues.values().forEach(sink -> sink.tryEmitNext(data));
        return queues;
    }

    public SinkKey subscribe(String topic) {
        var id = UUID.randomUUID().toString();
        var sink = addNewQueue(topic, id);
        return new SinkKey(id, sink);
    }

    private Sinks.Many<SensorData> addNewQueue(String topic, String id) {
        Sinks.Many<SensorData> sink = Sinks.many().unicast().onBackpressureBuffer();
        topicToMessageQueues.compute(topic, (key, queues) -> {
            if (queues == null) {
                queues = new HashMap<>();
            }
            queues.put(id, sink);
            return queues;
        });
        return sink;
    }

    public void unsubscribe(String topic, String id) {
        topicToMessageQueues.computeIfPresent(topic, (ignore, val) -> {
            val.remove(id);
            return val;
        });
    }

    public record SinkKey(String subscriberId, Sinks.Many<SensorData> sink) {}
}
