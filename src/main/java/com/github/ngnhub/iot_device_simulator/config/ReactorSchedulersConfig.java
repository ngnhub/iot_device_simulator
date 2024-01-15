package com.github.ngnhub.iot_device_simulator.config;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;


@Configuration
public class ReactorSchedulersConfig {

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public Scheduler singleThreadScheduler() {
        return Schedulers.single();
    }
}
