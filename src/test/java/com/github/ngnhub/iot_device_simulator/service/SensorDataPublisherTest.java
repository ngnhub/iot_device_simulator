package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.UUIDVerifier;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDataPublisher.DataConsumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
class SensorDataPublisherTest extends BaseTest {

    @Spy
    private ConcurrentHashMap<String, Map<String, DataConsumer>> topicToMessageQueues = new ConcurrentHashMap<>();
    @Spy
    private TestDataConsumer testDataConsumer = new TestDataConsumer();
    private SensorDataPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SensorDataPublisher(topicToMessageQueues);
    }

    @Test
    void shouldSendDataToAllConsumers() {
        // given
        var topic = "topic";
        var data = getSensorData(topic, "on");
        Map<String, DataConsumer> queues = new HashMap<>();
        var key1 = "key1";
        var key2 = "key2";
        DataConsumer consumer1 = testDataConsumer::doOnEvent;
        DataConsumer consumer2 = testDataConsumer::doOnEvent;
        queues.put(key1, consumer1);
        queues.put(key2, consumer2);
        topicToMessageQueues.put(topic, queues);

        // when
        publisher.publish(data);

        // then
        verify(testDataConsumer, times(2)).doOnEvent(data);
    }

    @Test
    void shouldAddNewMapToTopic() {
        // given
        var topic = "topic";
        DataConsumer consumer = testDataConsumer::doOnEvent;

        // when
        var key = publisher.subscribe(topic, consumer);

        // then
        assertTrue(UUIDVerifier.isUUID(key));
        assertEquals(topicToMessageQueues.get(topic).get(key), consumer);
    }

    @Test
    void shouldRemoveSinkFromMessageQueues() {
        // given
        var topic = "topic";
        var subscriberId = "id";
        DataConsumer consumer = testDataConsumer::doOnEvent;
        Map<String, DataConsumer> map = new HashMap<>();
        map.put(subscriberId, consumer);
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
        DataConsumer consumer = testDataConsumer::doOnEvent;

        // when
        var key = publisher.subscribe(topic, consumer);
        // then
        assertTrue(topicToMessageQueues.containsKey(topic));
        assertTrue(topicToMessageQueues.get(topic).containsKey(key));
        assertDoesNotThrow(() -> publisher.publish(data));

        // when
        publisher.unsubscribe(topic, key);
        // then
        assertTrue(topicToMessageQueues.containsKey(topic));
        assertFalse(topicToMessageQueues.get(topic).containsKey(key));
        assertDoesNotThrow(() -> publisher.publish(data));

        // when
        key = publisher.subscribe(topic, consumer);
        // then
        assertTrue(topicToMessageQueues.containsKey(topic));
        assertTrue(topicToMessageQueues.get(topic).containsKey(key));
        assertDoesNotThrow(() -> publisher.publish(data));
    }
}
