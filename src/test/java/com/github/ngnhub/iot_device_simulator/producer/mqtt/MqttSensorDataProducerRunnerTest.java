package com.github.ngnhub.iot_device_simulator.producer.mqtt;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;

import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CONNECTION_LOST;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class MqttSensorDataProducerRunnerTest extends BaseTest {

    private static final VirtualTimeScheduler SCHEDULER = VirtualTimeScheduler.getOrSet();

    @Mock
    private MqttSensorDataProducer publisher;
    @Mock
    private MqttConnectOptions options;
    private MqttSensorDataProducerRunner runner;

    @BeforeEach
    void setUp() {
        runner = spy(new MqttSensorDataProducerRunner(publisher, options));
    }

    @AfterAll
    static void afterAll() {
        SCHEDULER.dispose();
    }

    @Test
    void shouldCompleteSuccessfully() {
        // given
        Flux<Void> sentMessages = Flux.empty();
        when(publisher.subscribeAndProduce()).thenReturn(sentMessages);

        // when
        runner.runMqtt();

        // then
        StepVerifier.create(sentMessages).verifyComplete();
        verify(publisher).subscribeAndProduce();
        verify(publisher, never()).isConnected();
    }

    @Test
    void shouldRetryTwice() {
        // given
        when(options.getMaxReconnectDelay()).thenReturn(1000);
        Flux<Void> firstError = Flux.error(new MqttException(REASON_CODE_CONNECTION_LOST));
        Flux<Void> success = Flux.empty();
        when(publisher.subscribeAndProduce())
                .thenReturn(firstError)
                .thenReturn(success);
        when(publisher.isConnected())
                .thenReturn(false)
                .thenReturn(true);


        doReturn(SCHEDULER).when(runner).singleThreadScheduler();

        // when
        runner.runMqtt();
        long waitFor = 3000L;
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        StepVerifier.create(firstError).verifyError();
        StepVerifier.create(success).verifyComplete();

        verify(publisher, times(2)).subscribeAndProduce();
        verify(publisher, times(2)).isConnected();
    }

    @Test
    void shouldNotRetry() {
        // given
        Flux<Void> firstError = Flux.error(new RuntimeException());
        when(publisher.subscribeAndProduce()).thenReturn(firstError);

        // when
        runner.runMqtt();

        // then
        StepVerifier.create(firstError).verifyError();
        verify(publisher).subscribeAndProduce();
        verify(publisher, never()).isConnected();
    }
}
