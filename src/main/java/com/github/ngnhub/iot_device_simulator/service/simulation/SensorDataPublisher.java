package com.github.ngnhub.iot_device_simulator.service.simulation;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SensorDataPublisher {

    private final ConcurrentHashMap<String, Map<String, SensorDataListener>> topicToMessageQueues;

    public void publish(SensorData data) {
        var topic = data.getTopic();
        topicToMessageQueues.computeIfPresent(topic, (key, consumers) -> fanOut(data, consumers));
    }

    private Map<String, SensorDataListener> fanOut(SensorData data, Map<String, SensorDataListener> consumers) {
        consumers.values().forEach(listener -> {
            listener.onData(data);
            if (data.isErrored()) {
                listener.onData(data);
            }
        });
        return consumers;
    }

    public String subscribe(String topic, SensorDataListener consumer) {
        var id = UUID.randomUUID().toString();
        addNewConsumer(topic, id, consumer);
        return id;
    }

    private void addNewConsumer(String topic, String id, SensorDataListener consumer) {
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

    public interface SensorDataListener {
        void onData(SensorData data);

        void onError(SensorData data);
    }
}