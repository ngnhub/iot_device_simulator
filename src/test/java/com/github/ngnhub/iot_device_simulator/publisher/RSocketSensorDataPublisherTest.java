package com.github.ngnhub.iot_device_simulator.publisher;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;

@SpringBootTest
@Import(RSocketSensorDataPublisherTest.Config.class)
class RSocketSensorDataPublisherTest {

    @TestConfiguration
    public static class Config {
        @Bean
        public RSocketRequester rSocketRequester(@Value("${spring.rsocket.server.port}") int port) {
            RSocketStrategies strategies = RSocketStrategies.builder()
                    .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                    .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                    .build();
            return RSocketRequester.builder()
                    .rsocketStrategies(strategies)
                    .tcp("localhost", port);
        }
    }

    @Autowired
    private RSocketRequester requester;

    @AfterEach
    void tearDown() {
        requester.dispose();
    }

    @Test
    void stream() {
        // when
        Flux<SensorData> gpio = requester.route("subscribe/{sensorName}", "gpio")
                .retrieveFlux(SensorData.class)
                .take(3L);

        // then
        StepVerifier.create(gpio)
                .expectNextMatches(this::isGpioAndInRange)
                .expectNextMatches(this::isGpioAndInRange)
                .expectNextMatches(this::isGpioAndInRange)
                .verifyComplete();
    }

    private boolean isGpioAndInRange(SensorData<Double> val) {
        return val.getTopic().equals("gpio")
                && 0.0 <= val.getSensorData()
                && val.getSensorData() <= 1.0;
    }
}
