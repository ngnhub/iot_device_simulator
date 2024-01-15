package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SensorDataPublisher {

    private final ConcurrentHashMap<String, Map<String, DataConsumer>> topicToMessageQueues;

    public void publish(SensorData data) {
        var topic = data.getTopic();
        topicToMessageQueues.computeIfPresent(topic, (key, consumers) -> fanOut(data, consumers));
    }

    private Map<String, DataConsumer> fanOut(SensorData data, Map<String, DataConsumer> consumers) {
        consumers.values().forEach(listener -> listener.consume(data));
        return consumers;
    }

    public String subscribe(String topic, DataConsumer consumer) {
        var id = UUID.randomUUID().toString();
        addNewConsumer(topic, id, consumer);
        return id;
    }

    // TODO: 15.01.2024 https://projectreactor.io/docs/core/release/reference/#producing.create
    private void addNewConsumer(String topic, String id, DataConsumer consumer) {
        topicToMessageQueues.compute(topic, (key, queues) -> {
            if (queues == null) {
                queues = new HashMap<>();
            }
            queues.put(id, consumer);
            return queues;
        });
    }

    public void unsubscribe(String topic, String id) {
        topicToMessageQueues.computeIfPresent(topic, (ignore, val) -> {
            val.remove(id);
            return val;
        });
    }

    public record SinkKey(String subscriberId, Queue<SensorData> sink) {}

    public interface DataConsumer {
        void consume(SensorData data);
    }
}
