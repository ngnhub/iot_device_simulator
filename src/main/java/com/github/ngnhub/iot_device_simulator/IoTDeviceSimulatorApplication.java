package com.github.ngnhub.iot_device_simulator;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class IoTDeviceSimulatorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(IoTDeviceSimulatorApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
