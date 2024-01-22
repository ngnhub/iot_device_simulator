package com.github.ngnhub.iot_device_simulator.mapper;

import org.junit.jupiter.api.Test;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorDataFactoryTest {

    @Test
    void shouldCreateSensorData() {
        // given
        var gpio = gpio();
        var value = "0";

        // when
        var data = SensorDataFactory.create(gpio, value);

        // then
        assertEquals(gpio.topic(), data.topic());
        assertEquals("0", data.value());
        assertFalse(data.errored());
        assertNotNull(data.time());
    }

    @Test
    void shouldCreateSensorDataErrorData() {
        // given
        var topic = "test_topic";
        var testErrorMessage = "Test error message";

        // when
        var data = SensorDataFactory.create(topic, new RuntimeException(testErrorMessage));

        // then
        var expected = String.format("Error {%s}", testErrorMessage);
        assertEquals(topic, data.topic());
        assertEquals(expected, data.value());
        assertTrue(data.errored());
        assertNotNull(data.topic());
    }
}
