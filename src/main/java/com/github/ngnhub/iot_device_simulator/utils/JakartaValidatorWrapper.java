package com.github.ngnhub.iot_device_simulator.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class JakartaValidatorWrapper {

    private final Validator validator;

    public <T> void validate(T val) {
        Set<ConstraintViolation<T>> violations = validator.validate(val);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
