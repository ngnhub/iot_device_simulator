package com.github.ngnhub.iot_device_simulator.mqtt;

import com.github.ngnhub.iot_device_simulator.service.simulation.consuming.SensorDataSwitcher;
import com.github.ngnhub.iot_device_simulator.service.simulation.publishing.SensorDataPublisher;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class MqttSensorDataConsumersInitializerTest {

    @Mock
    private MqttClient mqttClient;
    @Mock
    private SensorDataSwitcher switcher;
    @Mock
    private Flux<String> topicsOfSwitchableDevices;
    @Mock
    private SensorDataPublisher publisher;
    private MqttSensorDataConsumersInitializer initializer;


    @BeforeEach
    void setUp() {
        initializer = new MqttSensorDataConsumersInitializer(
                mqttClient,
                switcher,
                topicsOfSwitchableDevices,
                publisher
        );

    }

    @Test
    void initSubscriptionsOnSwitchableTopics() {

    }
}