package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SensorDataPublisherTest {

    private ConcurrentHashMap<String, Map<String, Sinks.Many<SensorData>>> topicToMessageQueues;
    private SensorDataPublisher publisher;

    @BeforeEach
    void setUp() {
        topicToMessageQueues = new ConcurrentHashMap<>();
        publisher = new SensorDataPublisher(topicToMessageQueues);
    }

    @Test
    void shouldFillAllSinks() {
        // given
        var topic = "topic";
        var data = getSensorData(topic, "on");
        Map<String, Sinks.Many<SensorData>> queues = new HashMap<>();
        var key1 = "key1";
        var key2 = "key2";
        Sinks.Many<SensorData> sink1 = Sinks.many().unicast().onBackpressureBuffer();
        Sinks.Many<SensorData> sink2 = Sinks.many().unicast().onBackpressureBuffer();
        queues.put(key1, sink1);
        queues.put(key2, sink2);
        topicToMessageQueues.put(topic, queues);

        // when
        publisher.publish(data);
        sink1.tryEmitComplete();
        sink2.tryEmitComplete();

        // then
        StepVerifier.create(sink1.asFlux())
                .expectNext(data)
                .verifyComplete();
        StepVerifier.create(sink2.asFlux())
                .expectNext(data)
                .verifyComplete();
    }

    @Test
    void shouldAddNewSinkToMessageQueues() {
        // given
        var topic = "topic";

        // when
        var key = publisher.subscribe(topic);

        // then
        assertNotNull(key.subscriberId());
        assertNotNull(key.sink());
        assertEquals(topicToMessageQueues.get(topic).get(key.subscriberId()), key.sink());
    }

    @Test
    void shouldRemoveSinkFromMessageQueues() {
        // given
        var topic = "topic";
        var subscriberId = "id";
        Map<String, Sinks.Many<SensorData>> map = new HashMap<>();
        map.put(subscriberId, Sinks.many().unicast().onBackpressureBuffer());
        topicToMessageQueues.put(topic, map);
        assertTrue(topicToMessageQueues.get(topic).containsKey(subscriberId));

        // when
        publisher.unsubscribe(topic, subscriberId);

        // then
        assertFalse(topicToMessageQueues.get(topic).containsKey(subscriberId));
    }

    @Test
    void shouldSubscribeUnsubscribeAndSubscribeSuccessfully() {
        // given
        var topic = "topic";
        var data = getSensorData(topic, "on");

        // when
        var key = publisher.subscribe(topic);
        // then
        assertTrue(topicToMessageQueues.containsKey(topic));
        assertTrue(topicToMessageQueues.get(topic).containsKey(key.subscriberId()));
        assertDoesNotThrow(() -> publisher.publish(data));

        // when
        publisher.unsubscribe(topic, key.subscriberId());
        // then
        assertTrue(topicToMessageQueues.containsKey(topic));
        assertFalse(topicToMessageQueues.get(topic).containsKey(key.subscriberId()));
        assertDoesNotThrow(() -> publisher.publish(data));

        // when
        key = publisher.subscribe(topic);
        // then
        assertTrue(topicToMessageQueues.containsKey(topic));
        assertTrue(topicToMessageQueues.get(topic).containsKey(key.subscriberId()));
        assertDoesNotThrow(() -> publisher.publish(data));
    }
}
