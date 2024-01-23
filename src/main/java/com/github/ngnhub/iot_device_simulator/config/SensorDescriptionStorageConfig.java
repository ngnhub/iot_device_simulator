package com.github.ngnhub.iot_device_simulator.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ngnhub.iot_device_simulator.model.SensorDescription;
import com.github.ngnhub.iot_device_simulator.utils.SensorDescriptionValidator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class SensorDescriptionStorageConfig {

    @Bean
    public Map<String, SensorDescription> descriptions(@Value("${sensor-descriptor-path}") String descriptorPath,
                                                       SensorDescriptionValidator validator) throws IOException {
        var file = ResourceUtils.getFile(descriptorPath);
        var mapper = new ObjectMapper();
        List<SensorDescription> descriptionList = mapper.readValue(file, new TypeReference<>() {});
        descriptionList.forEach(validator::validate);
        return descriptionList.stream()
                .collect(Collectors.toMap(SensorDescription::topic, Function.identity()));
    }
}
