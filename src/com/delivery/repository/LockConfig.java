package com.delivery.repository;

public class LockConfig {
    private static LockMechanism currentMechanism = LockMechanism.NO_LOCK;

    public static LockMechanism getMechanism() {
        return currentMechanism;
    }

    public static void setMechanism(LockMechanism mechanism) {
        currentMechanism = mechanism;
    }
}
