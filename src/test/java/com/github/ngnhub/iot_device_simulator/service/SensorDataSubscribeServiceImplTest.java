package com.github.ngnhub.iot_device_simulator.service;

import com.github.ngnhub.iot_device_simulator.config.InternalProps;
import com.github.ngnhub.iot_device_simulator.model.SensorData;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher.SensorDataListener;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorDataSubscribeServiceImplTest {

    @Mock
    private SensorDataPublisherImpl publisher;
    private SensorDataSubscribeServiceImpl service;

    @BeforeEach
    void setUp() {
        var subscriber = new InternalProps.Subscriber();
        subscriber.setRetryAttempts(2);
        subscriber.setRetryDelayMillis(1000L);
        InternalProps internalProps = new InternalProps();
        internalProps.setSubscriber(subscriber);
        service = new SensorDataSubscribeServiceImpl(publisher, internalProps);
    }

    @Test
    void shouldConsumeSensorDataAndUnsubscribeFinally() {
        // given
        var topic = "topic";
        SensorData data1 = getSensorData(topic, "1");
        SensorData data2 = getSensorData(topic, "2");
        SensorData data3 = getSensorData(topic, "3");
        var keyId = "id";
        when(publisher.subscribe(eq(topic), any())).thenAnswer(a -> {
            SensorDataListener argument = a.getArgument(1);
            argument.onData(data1);
            argument.onData(data2);
            argument.onData(data3);
            return keyId;
        });

        // when
        Flux<SensorData> result = service.subscribeOn(topic).take(3);

        // then
        StepVerifier.create(result)
                .expectNext(data1)
                .expectNext(data2)
                .expectNext(data3)
                .verifyComplete();
        verify(publisher).unsubscribe(topic, keyId);
    }

    @Test
    void shouldResubscribeIfSinkOverflow() {
        // given
        var topic = "topic";
        SensorData data = getSensorData(topic, "1");
        var keyId = "id";
        when(publisher.subscribe(eq(topic), any()))
                .thenThrow(Exceptions.failWithOverflow())
                .thenThrow(Exceptions.failWithOverflow())
                .thenAnswer(a -> {
                    SensorDataListener argument = a.getArgument(1);
                    argument.onData(data);
                    return keyId;
                });

        // when
        Flux<SensorData> result = service.subscribeOn(topic).take(1);

        // then
        StepVerifier.create(result)
                .expectNext(data)
                .verifyComplete();
        verify(publisher, times(3)).subscribe(eq(topic), any());
    }

    @Test
    void shouldNotResubscribeIfSinkOverflowedMoreThanAttemptsNumber() {
        // given
        var topic = "topic";
        SensorData data = getSensorData(topic, "1");
        when(publisher.subscribe(eq(topic), any()))
                .thenThrow(Exceptions.failWithOverflow())
                .thenThrow(Exceptions.failWithOverflow())
                .thenThrow(Exceptions.failWithOverflow());

        // when
        Flux<SensorData> result = service.subscribeOn(topic).take(1);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(err -> Exceptions.isOverflow(err)
                        && ("Subscription on topic failed after 2 retries." +
                        " Consumer processes data too slowly").equals(err.getMessage()))
                .verify();
        verify(publisher, times(3)).subscribe(eq(topic), any());
    }

    @Test
    void shouldNotResubscribeAfterOtherExceptions() {
        // given
        var topic = "topic";
        when(publisher.subscribe(eq(topic), any()))
                .thenThrow(RuntimeException.class);

        // when
        Flux<SensorData> result = service.subscribeOn(topic).take(1);

        // then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
        verify(publisher).subscribe(eq(topic), any());
    }

    @Test
    void shouldReturnAndUnsubscribeOnError() {
        // given
        var topic = "topic";
        SensorData data1 = getSensorData(topic, "1");
        SensorData data2 = getSensorData(topic, "2");
        SensorData data3 = getSensorData(topic, "3");
        var keyId = "id";
        when(publisher.subscribe(eq(topic), any())).thenAnswer(a -> {
            SensorDataListener argument = a.getArgument(1);
            argument.onData(data1);
            argument.onData(data2);
            argument.onData(data3);
            argument.onError(data3);
            return keyId;
        });

        // when
        Flux<SensorData> result = service.subscribeOn(topic).take(3);

        // then
        StepVerifier.create(result)
                .expectNext(data1)
                .expectNext(data2)
                .expectNext(data3)
                .verifyComplete();
        verify(publisher).unsubscribe(topic, keyId);
    }
}
