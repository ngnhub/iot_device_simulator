package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.SensorDataPublisher.SinkKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorDataSubscribeServiceTest {

    @Mock
    private SensorDataPublisher publisher;
    private SensorDataSubscribeService service;

    @BeforeEach
    void setUp() {
        service = new SensorDataSubscribeService(publisher);
    }

    @Test
    void shouldConsumeSensorDataAndUnsubscribeFinally() {
        // given
        var topic = "topic";
        SensorData data1 = getSensorData(topic, "1");
        SensorData data2 = getSensorData(topic, "2");
        SensorData data3 = getSensorData(topic, "3");
        Sinks.Many<SensorData> sink = Sinks.many().unicast().onBackpressureBuffer();
        var keyId = "id";
        var sinkKey = new SinkKey(keyId, sink);
        when(publisher.subscribe(topic)).thenReturn(sinkKey);

        // when
        Flux<SensorData> result = service.subscribeOn(topic);
        sink.tryEmitNext(data1);
        sink.tryEmitNext(data2);
        sink.tryEmitNext(data3);
        sink.tryEmitComplete();

        // then
        StepVerifier.create(result)
                .expectNext(data1)
                .expectNext(data2)
                .expectNext(data3)
                .verifyComplete();
        verify(publisher).unsubscribe(topic, keyId);
    }

    @Test
    void shouldUnsubscribeFinallyIfErrored() {
        // given
        var topic = "topic";
        Sinks.Many<SensorData> sink = Sinks.many().unicast().onBackpressureBuffer();
        var keyId = "id";
        var sinkKey = new SinkKey(keyId, sink);
        when(publisher.subscribe(topic)).thenReturn(sinkKey);

        // when
        Flux<SensorData> result = service.subscribeOn(topic);
        sink.tryEmitError(new RuntimeException("Simulate error"));

        // then
        StepVerifier.create(result)
                .verifyErrorMatches(err -> err instanceof RuntimeException && "Simulate error".equals( err.getMessage()));
        verify(publisher).unsubscribe(topic, keyId);
    }
}