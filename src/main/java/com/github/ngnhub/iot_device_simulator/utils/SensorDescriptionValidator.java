package com.github.ngnhub.iot_device_simulator.utils;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import static com.github.ngnhub.iot_device_simulator.utils.SensorValueTypes.STRING;

@Component
@RequiredArgsConstructor
public class SensorDescriptionValidator {

    private final JakartaValidatorWrapper jakartaValidator;

    public void validate(SensorDescription description) {
        jakartaValidator.validate(description);
        validateStringHasPossibleValues(description);
        validatePossibleValuesMatchType(description);
    }

    private void validateStringHasPossibleValues(SensorDescription description) {
        if (STRING.equals(description.type()) && CollectionUtils.isEmpty(description.possibleValues())) {
            throw new ConstraintViolationException(
                    "Possible values can't be empty for the string type. Topic: " + description.topic(), null);
        }
    }

    private void validatePossibleValuesMatchType(SensorDescription description) {
        var values = description.possibleValues();
        if (ObjectUtils.isEmpty(values)) {
            return;
        }
        values.forEach(val -> {
            var possibleValueType = val.getClass().getSimpleName();
            if (!description.type().equals(possibleValueType)) {
                throw new ConstraintViolationException(
                        "Possible values have invalid type. Topic: " + description.topic(), null);
            }
        });
    }
}
