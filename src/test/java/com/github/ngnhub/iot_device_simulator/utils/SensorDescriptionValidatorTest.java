package com.github.ngnhub.iot_device_simulator.utils;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static com.github.ngnhub.iot_device_simulator.utils.SensorValueTypes.DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SensorDescriptionValidatorTest {

    @Mock
    private JakartaValidatorWrapper facade;
    private SensorDescriptionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SensorDescriptionValidator(facade);
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
}
