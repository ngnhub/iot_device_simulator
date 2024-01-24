package com.github.ngnhub.iot_device_simulator;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

// TODO: 24.01.2024 github pub
// TODO: 24.01.2024 remove basepath slash requirement
// TODO: 18.01.2024 github version
// TODO: 24.01.2024 volume for sensors
@SpringBootApplication
public class IoTDeviceSimulatorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(IoTDeviceSimulatorApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
