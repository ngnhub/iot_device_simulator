package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.service.simulation.SensorDescriptionStorage;
import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MqttSensorDataConsumersInitializerTest {

    @Mock
    private MqttClient mqttClient;
    @Mock
    private SensorDataSwitcher switcher;
    @Mock
    private SensorDescriptionStorage storage;
    private MqttSensorDataConsumersInitializer initializer;


    @BeforeEach
    void setUp() {
        initializer = new MqttSensorDataConsumersInitializer(
                mqttClient,
                switcher,
                storage
        );
    }

    @Test
    void shouldConvertValueAndSendToSwitcher() {
        // given
        var expectedValue = 1.0;
        SensorDescription fan = fan();
        var data = getSensorData(fan.topic(), String.valueOf(expectedValue));
        when(storage.getOnlySwitchable()).thenReturn(Flux.just(fan));
        when(switcher.switchOn(eq(fan.switcher()), any())).thenReturn(Mono.just(data));

        // when
        Flux<IMqttMessageListener> listenerFlux = initializer.initSubscriptionsOnSwitchableTopics()
                .doOnNext(listener -> {
                    try {
                        listener.messageArrived(
                                fan.switcher(),
                                new MqttMessage(String.valueOf(expectedValue).getBytes())
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // then
        StepVerifier.create(listenerFlux)
                .assertNext(listener -> {
                    try {
                        verify(mqttClient).subscribe(fan.switcher(), listener);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                })
                .verifyComplete();
        verify(switcher).switchOn(fan.switcher(), expectedValue);
    }

    @Test
    void shouldThrowAnErrorAndNotSendToSwitcherIfInvalidFormat() throws Exception {
        // given
        var errored = "value";
        SensorDescription fan = fan();
        when(storage.getOnlySwitchable()).thenReturn(Flux.just(fan));
        doAnswer(a -> {
            IMqttMessageListener argument = a.getArgument(1);
            argument.messageArrived(fan.switcher(), new MqttMessage(errored.getBytes()));
            return null;
        }).when(mqttClient).subscribe(any(String.class), any(IMqttMessageListener.class));

        // when
        Flux<IMqttMessageListener> result = initializer.initSubscriptionsOnSwitchableTopics()
                .doOnNext(listener -> {
                    try {
                        listener.messageArrived(
                                fan.switcher(),
                                new MqttMessage(errored.getBytes())
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        // then
        StepVerifier.create(result)
                .expectNextCount(1L)
                .verifyComplete();
        verify(switcher, never()).switchOn(any(), any());
    }
}