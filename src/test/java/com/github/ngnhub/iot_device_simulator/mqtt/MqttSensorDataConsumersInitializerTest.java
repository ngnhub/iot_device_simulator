package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDataFactory.getSensorData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MqttSensorDataConsumersInitializerTest {

    @Mock
    private MqttClient mqttClient;
    @Mock
    private SensorDataSwitcher switcher;
    private Flux<String> topicsOfSwitchableDevices;
    @Mock
    private SensorDataPublisher publisher;
    @Captor
    private ArgumentCaptor<IMqttMessageListener> messageListenerCaptor;
    private MqttSensorDataConsumersInitializer initializer;


    @BeforeEach
    void setUp() {
        topicsOfSwitchableDevices = Flux.just("topic");
        initializer = new MqttSensorDataConsumersInitializer(
                mqttClient,
                switcher,
                topicsOfSwitchableDevices,
                publisher
        );
    }

    @Test
    void initSubscriptionsOnSwitchableTopics() throws Exception {
        // given
        var expectedValue = "value";
        var topic = "topic";
        var data = getSensorData(topic, expectedValue);
        when(switcher.switchOn(eq(topic), any())).thenReturn(Mono.just(data));
        // when
        initializer.initSubscriptionsOnSwitchableTopics().subscribe();

        // then
        verify(mqttClient).subscribe(eq(topic), messageListenerCaptor.capture());
        messageListenerCaptor.getValue().messageArrived(topic, new MqttMessage(expectedValue.getBytes()));
        verify(switcher).switchOn(topic, expectedValue);
        verify(publisher).publish(data);
    }
}