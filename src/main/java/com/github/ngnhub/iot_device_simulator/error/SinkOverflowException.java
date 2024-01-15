package com.github.ngnhub.iot_device_simulator.error;

public class SinkOverflowException extends RuntimeException {

    public SinkOverflowException() {
        super();
    }

    public SinkOverflowException(String message) {
        super(message);
    }
}
