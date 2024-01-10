package com.github.ngnhub.iot_device_simulator.utils;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SensorDescriptionValidator {

    private final ValidationFacade jakartaValidator;

    public void validate(SensorDescription description) {
        jakartaValidator.validate(description);
        List<Object> values = description.possibleValues();
        if (!ObjectUtils.isEmpty(values)) {
            validatePossibleValues(description);
        }
    }

    // TODO: 10.01.2024 if null min max can't be there
    private void validatePossibleValues(SensorDescription description) {
        var values = description.possibleValues();
        if (ObjectUtils.isEmpty(values)) {
            return;
        }
        values.forEach(val -> {
            var possibleValueType = val.getClass().getSimpleName();
            if (!description.type().equals(possibleValueType)) {
                throw new ConstraintViolationException(
                        "Possible values invalid type; topic: " + description.topic(), null);
            }
        });
    }
}
