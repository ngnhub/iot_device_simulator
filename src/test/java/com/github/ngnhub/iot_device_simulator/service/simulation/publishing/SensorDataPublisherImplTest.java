package com.github.ngnhub.iot_device_simulator.service.simulation.publishing;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.UUIDVerifier;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher.SensorDataListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
class SensorDataPublisherImplTest extends BaseTest {

    @Spy
    private ConcurrentHashMap<String, ConcurrentHashMap<String, SensorDataListener>> topicToMessageQueues = new ConcurrentHashMap<>();
    private SensorDataPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new SensorDataPublisherImpl(topicToMessageQueues);
    }

    @Test
    void shouldSendDataToAllConsumers() {
        // given
        var topic = "topic";
        var data = getSensorData(topic, "on");
        ConcurrentHashMap<String, SensorDataListener> queues = new ConcurrentHashMap<>();
        var key1 = "key1";
        var key2 = "key2";
        SensorDataListener consumer1 = Mockito.spy(new TestSensorDataListener());
        SensorDataListener consumer2 = spy(new TestSensorDataListener());
        queues.put(key1, consumer1);
        queues.put(key2, consumer2);
        topicToMessageQueues.put(topic, queues);

        // when
        publisher.publish(data);

        // then
        verify(consumer1, times(1)).onData(data);
        verify(consumer2, times(1)).onData(data);
    }

    @Test
    void shouldSendErrored() {
        // given
        var topic = "topic";
        var data = getSensorData(topic, "on").toBuilder().errored(true).build();
        ConcurrentHashMap<String, SensorDataListener> queues = new ConcurrentHashMap<>();
        var key1 = "key1";
        var key2 = "key2";
        SensorDataListener consumer1 = spy(new TestSensorDataListener());
        SensorDataListener consumer2 = spy(new TestSensorDataListener());
        queues.put(key1, consumer1);
        queues.put(key2, consumer2);
        topicToMessageQueues.put(topic, queues);

        // when
        publisher.publish(data);

        // then
        verify(consumer1, times(1)).onError(data);
        verify(consumer2, times(1)).onError(data);
    }

    @Test
    void shouldAddNewMapToTopic() {
        // given
        var topic = "topic";
        SensorDataListener consumer = spy(new TestSensorDataListener());

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
        SensorDataListener consumer = spy(new TestSensorDataListener());
        ConcurrentHashMap<String, SensorDataListener> queues = new ConcurrentHashMap<>();
        queues.put(subscriberId, consumer);
        topicToMessageQueues.put(topic, queues);
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
        SensorDataListener consumer = spy(new TestSensorDataListener());

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
