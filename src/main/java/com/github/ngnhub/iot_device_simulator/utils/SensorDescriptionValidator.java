package com.github.ngnhub.iot_device_simulator.utils;

import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Component
@RequiredArgsConstructor
public class SensorDescriptionValidator {

    private final JakartaValidatorWrapper jakartaValidator;

    public void validate(SensorDescription description) {
        jakartaValidator.validate(description);
        if (description.switcher() != null) {
            jakartaValidator.validate(description.switcher());
        }
        validateGeneratorHasPossibleValues(description);
        validatePossibleValuesMatchType(description);
        validateNotSwitcherAndHastNotInterval(description);
    }

    private void validateGeneratorHasPossibleValues(SensorDescription description) {
        if (description.switcher() == null
                && CollectionUtils.isEmpty(description.possibleValues())
                && description.min() == null
                && description.max() == null) {
            throwError("Possible values or min and max must be present for non-SV types. Topic: ", description.topic());
        }
    }

    private void validatePossibleValuesMatchType(SensorDescription description) {
        var values = description.possibleValues();
        if (ObjectUtils.isEmpty(values)) {
            return;
        }
        values.forEach(val -> {
            if (!description.type().getAClass().isInstance(val)) {
                throwError("Possible values have invalid type. Topic: ", description.topic());
            }
        });
    }

    private void validateNotSwitcherAndHastNotInterval(SensorDescription description) {
        if (description.switcher() == null && description.interval() == null) {
            throwError("Interval must not be null if not switchable. Topic: ", description.topic());
        }
    }

    private void throwError(String message, String topic) {
        throw new ConstraintViolationException(message + topic, null);
    }
}
