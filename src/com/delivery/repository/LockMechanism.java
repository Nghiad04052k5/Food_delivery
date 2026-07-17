package com.delivery.repository;

public enum LockMechanism {
    NO_LOCK,
    SYNCHRONIZED,
    FILE_LOCK,
    OPTIMISTIC
}
