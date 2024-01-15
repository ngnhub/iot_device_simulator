package com.github.ngnhub.iot_device_simulator.producer.rsocket;

import com.github.ngnhub.iot_device_simulator.model.SensorData;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.util.CollectionUtils;

import static com.github.ngnhub.iot_device_simulator.factory.TestSensorDescriptionFactory.gpio;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Disabled // FIXME: 15.01.2024
@SpringBootTest
@Import(RSocketSensorDataProducerTest.Config.class)
class RSocketSensorDataProducerTest {

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

    @Test
    void testStream() {
        // when
        Flux<SensorData> gpio = requester.route("subscribe/{topic}", "gpio")
                .retrieveFlux(SensorData.class)
                .take(3L);

        // then
        StepVerifier.create(gpio)
                .expectNextMatches(this::isGpioAndInRange)
                .expectNextMatches(this::isGpioAndInRange)
                .expectNextMatches(this::isGpioAndInRange)
                .verifyComplete();
    }

    private boolean isGpioAndInRange(SensorData val) {
        var gpio = gpio();
        var possibleValues = gpio.possibleValues();
        assertFalse(CollectionUtils.isEmpty(possibleValues));
        return val.getTopic().equals("gpio")
                && possibleValues.contains(val.getSensorData());
    }

    @Test
    void testStreamValues() {
        // when
        Flux<String> gpio = requester.route("subscribe/value/{topic}", "gpio")
                .retrieveFlux(String.class)
                .take(3L);

        // then
        var possibleValues = gpio().possibleValues();
        assertFalse(CollectionUtils.isEmpty(possibleValues));
        StepVerifier.create(gpio)
                .expectNextMatches(possibleValues::contains)
                .expectNextMatches(possibleValues::contains)
                .expectNextMatches(possibleValues::contains)
                .verifyComplete();
    }
}
