package com.github.ngnhub.iot_device_simulator.service.simulation.publishing;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SensorDataPublisher {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, SensorDataListener>> topicToMessageListeners;

    public void publish(SensorData data) {
        var topic = data.topic();
        topicToMessageListeners.computeIfPresent(topic, (key, consumers) -> fanOut(data, consumers));
    }

    private ConcurrentHashMap<String, SensorDataListener> fanOut(SensorData data,
                                                                 ConcurrentHashMap<String, SensorDataListener> consumers) {
        consumers.values().forEach(listener -> {
            listener.onData(data);
            if (data.errored()) {
                listener.onError(data);
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
        topicToMessageListeners.compute(topic, (key, queues) -> {
            if (queues == null) {
                queues = new ConcurrentHashMap<>();
            }
            queues.put(id, consumer);
            return queues;
        });
    }

    public void unsubscribe(String topic, String id) {
        topicToMessageListeners.computeIfPresent(topic, (ignore, val) -> {
            val.remove(id);
            return val;
        });
    }

    public interface SensorDataListener {
        void onData(SensorData data);

        void onError(SensorData data);
    }
}
