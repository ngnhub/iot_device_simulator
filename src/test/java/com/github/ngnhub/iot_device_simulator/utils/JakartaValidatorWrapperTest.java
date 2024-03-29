package com.github.ngnhub.iot_device_simulator.utils;

import com.github.ngnhub.iot_device_simulator.BaseTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JakartaValidatorWrapperTest extends BaseTest {

    @Mock
    private Validator validator;
    private JakartaValidatorWrapper facade;

    @BeforeEach
    void setUp() {
        facade = new JakartaValidatorWrapper(validator);
    }

    @Test
    void validate() {
        // given
        ConstraintViolation<Object> mock = mock(ConstraintViolation.class);
        when(validator.validate(any())).thenReturn(Set.of(mock));

        // when
        ConstraintViolationException exc = assertThrows(
                ConstraintViolationException.class,
                () -> facade.validate("bean")
        );

        // then
        assertEquals(Set.of(mock), exc.getConstraintViolations());
    }
}
