package com.github.ngnhub.iot_device_simulator.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.utils.SensorDescriptionValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class SensorDescriptionStorageConfig {

    @Bean
    public Map<String, SensorDescription> descriptions(SensorDescriptionProps props,
                                                       SensorDescriptionValidator validator) throws IOException {
        return getDescriptionFile(props).stream()
                .peek(validator::validate)
                .collect(Collectors.toMap(SensorDescription::topic, Function.identity()));
    }

    private List<SensorDescription> getDescriptionFile(SensorDescriptionProps props) throws IOException {
        var mapper = new ObjectMapper();
        if (ObjectUtils.isEmpty(props.getPath())) {
            var stream = getClass().getClassLoader().getResourceAsStream(props.getDefaultResource());
            return mapper.readValue(stream, new TypeReference<>() {});
        }
        var file = new File(props.getPath());
        return mapper.readValue(file, new TypeReference<>() {});
    }
}
