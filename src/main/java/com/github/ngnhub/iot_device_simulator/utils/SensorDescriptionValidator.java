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
        validateSwitcherAndContainsInitValueAndPossibleValues(description);
    }

    private void validateStringHasPossibleValues(SensorDescription description) {
        if (STRING == description.type() && CollectionUtils.isEmpty(description.possibleValues())) {
            throwError("Possible values can't be empty for the string type. Topic: ", description.topic());
        }
    }

    private void validatePossibleValuesMatchType(SensorDescription description) {
        var values = description.possibleValues();
        if (ObjectUtils.isEmpty(values)) {
            return;
        }
        values.forEach(val -> {
            var possibleValueType = val.getClass().getSimpleName();
            if (!description.type().getTypeSimpleClassName().equals(possibleValueType)) {
                throwError("Possible values have invalid type. Topic: ", description.topic());
            }
        });
    }

    private void validateSwitcherAndContainsInitValueAndPossibleValues(SensorDescription description) {
        if (description.switcher()) {
            var initValue = description.initValue();
            if (initValue == null) {
                throwError("Init value must be provided for the switcher. Topic: ", description.topic());
            }
            var initValueTypeName = initValue.getClass().getSimpleName();
            var sensorTypeName = description.type().getTypeSimpleClassName();
            if (!initValueTypeName.equals(sensorTypeName)) {
                throwError("Init value type is not matched to sensor type. Topic: ", description.topic());
            }
        }
    }

    private void throwError(String message, String topic) {
        throw new ConstraintViolationException(message + topic, null);
    }
}
