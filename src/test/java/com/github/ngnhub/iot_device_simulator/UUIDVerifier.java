package com.github.ngnhub.iot_device_simulator;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class UUIDVerifier {

    public static boolean isUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
