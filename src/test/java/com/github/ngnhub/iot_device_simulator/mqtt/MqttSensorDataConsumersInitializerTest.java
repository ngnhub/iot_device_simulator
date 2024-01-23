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
        var expectedValue = String.valueOf(1.0);
        var expectedBytes = expectedValue.getBytes();
        SensorDescription fan = fan();
        var data = getSensorData(fan.topic(), expectedValue);
        when(storage.getOnlySwitchable()).thenReturn(Flux.just(fan));
        when(switcher.switchOn(eq(fan.switcher()), any())).thenReturn(Mono.just(data));

        // when
        Flux<IMqttMessageListener> listenerFlux = initializer.initSubscriptionsOnSwitchableTopics()
                .doOnNext(listener -> {
                    try {
                        listener.messageArrived(
                                fan.switcher(),
                                new MqttMessage((expectedValue).getBytes())
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
        verify(switcher).switchOn(fan.switcher(), expectedBytes);
    }

}