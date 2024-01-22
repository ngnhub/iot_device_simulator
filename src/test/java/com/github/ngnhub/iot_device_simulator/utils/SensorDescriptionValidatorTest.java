package com.github.ngnhub.iot_device_simulator.utils;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.fan;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.temperature;
import static com.github.ngnhub.iot_device_simulator.utils.SensorValueType.DOUBLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensorDescriptionValidatorTest extends BaseTest {

    @Mock
    private JakartaValidatorWrapper facade;
    private SensorDescriptionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SensorDescriptionValidator(facade);
    }

    @Test
    void shouldPassValidation() {
        // given
        var gpio = gpio();

        // then
        assertDoesNotThrow(() -> validator.validate(gpio));
    }

    @Test
    void shouldThrowExceptionWhenPossibleValuesNotPresentForStringType() {
        // given
        SensorDescription description = gpio()
                .toBuilder()
                .possibleValues(null).build();

        // when
        ConstraintViolationException exc =
                assertThrows(ConstraintViolationException.class, () -> validator.validate(description));

        // then
        assertEquals(
                "Possible values can't be empty for the string type. Topic: " + gpio().topic(),
                exc.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPossibleValuesNotMatchToType() {
        // given
        SensorDescription description = gpio()
                .toBuilder()
                .type(DOUBLE)
                .build();

        // when
        ConstraintViolationException exc =
                assertThrows(ConstraintViolationException.class, () -> validator.validate(description));

        // then
        assertEquals("Possible values have invalid type. Topic: " + gpio().topic(), exc.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSwitcherDoesNotHaveInitValue() {
        // given
        SensorDescription description = fan()
                .toBuilder()
                .initValue(null)
                .build();

        // when
        ConstraintViolationException exc =
                assertThrows(ConstraintViolationException.class, () -> validator.validate(description));

        // then
        assertEquals("Init value must be provided for the switcher. Topic: fan", exc.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInitValueTypeInvalid() {
        // given
        SensorDescription description = fan()
                .toBuilder()
                .initValue("1.0")
                .build();

        // when
        ConstraintViolationException exc =
                assertThrows(ConstraintViolationException.class, () -> validator.validate(description));

        // then
        assertEquals("Init value type is not matched to sensor type. Topic: fan", exc.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNotSwitcherHasNoInterval() {
        // given
        SensorDescription description = temperature()
                .toBuilder()
                .interval(null)
                .build();

        // when
        ConstraintViolationException exc =
                assertThrows(ConstraintViolationException.class, () -> validator.validate(description));

        // then
        assertEquals("Interval must not be null if not switchable. Topic: temperature", exc.getMessage());
    }
}
