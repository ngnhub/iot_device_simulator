package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.mqtt.impl.MqttSensorDataConsumersInitializerImpl;
import com.github.ngnhub.iot_device_simulator.mqtt.impl.MqttSensorDataProducerImpl;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
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


class MqttSensorDataProducerImplRunnerTest extends BaseTest {

    private static final VirtualTimeScheduler SCHEDULER = VirtualTimeScheduler.getOrSet();

    @Mock
    private MqttSensorDataProducerImpl producer;
    @Mock
    private MqttSensorDataConsumersInitializerImpl consumer;
    @Mock
    private MqttConnectOptions options;
    @Mock
    private MqttClient client;
    private MqttSensorDataProducerRunner runner;

    @BeforeEach
    void setUp() {
        runner = spy(new MqttSensorDataProducerRunner(producer, consumer, options, client));
    }

    @AfterAll
    static void afterAll() {
        SCHEDULER.dispose();
    }

    @Test
    void shouldCompleteSuccessfully() {
        // given
        Flux<Void> sentMessages = Flux.empty();
        Flux<IMqttMessageListener> subscriptions = Flux.empty();
        when(producer.initProduce()).thenReturn(sentMessages);
        when(consumer.initSubscriptionsOnSwitchableTopics()).thenReturn(subscriptions);

        // when
        runner.init();

        // then
        StepVerifier.create(sentMessages).verifyComplete();
        verify(producer).initProduce();
        verify(consumer).initSubscriptionsOnSwitchableTopics();
        verify(client, never()).isConnected();
    }

    @Test
    void shouldRetryTwice() {
        // given
        Flux<IMqttMessageListener> subscriptions = Flux.empty();
        when(consumer.initSubscriptionsOnSwitchableTopics()).thenReturn(subscriptions);
        when(options.getMaxReconnectDelay()).thenReturn(1000);
        Flux<Void> firstError = Flux.error(new MqttException(REASON_CODE_CONNECTION_LOST));
        Flux<Void> success = Flux.empty();
        when(producer.initProduce())
                .thenReturn(firstError)
                .thenReturn(success);
        when(client.isConnected())
                .thenReturn(false)
                .thenReturn(true);


        doReturn(SCHEDULER).when(runner).singleThreadScheduler();

        // when
        runner.init();
        long waitFor = 3000L;
        SCHEDULER.advanceTimeBy(Duration.ofMillis(waitFor));

        // then
        StepVerifier.create(firstError).verifyError();
        StepVerifier.create(success).verifyComplete();

        verify(producer, times(2)).initProduce();
        verify(consumer, times(2)).initSubscriptionsOnSwitchableTopics();
        verify(client, times(2)).isConnected();
    }

    @Test
    void shouldNotRetry() {
        // given
        Flux<IMqttMessageListener> subscriptions = Flux.empty();
        when(consumer.initSubscriptionsOnSwitchableTopics()).thenReturn(subscriptions);
        Flux<Void> firstError = Flux.error(new RuntimeException());
        when(producer.initProduce()).thenReturn(firstError);

        // when
        runner.init();

        // then
        StepVerifier.create(firstError).verifyError();
        verify(producer).initProduce();
        verify(consumer).initSubscriptionsOnSwitchableTopics();
        verify(client, never()).isConnected();
    }

    @Test
    void shouldDisconnect() throws Exception {
        // when
        runner.tearDown();

        // then
        verify(client).disconnect();
    }
}
