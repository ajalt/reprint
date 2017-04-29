package com.github.ajalt.reprint.core;

/**
 * General categories for authentication failures.
 */
public enum AuthenticationFailureReason {
    /**
     * The device does not have a fingerprint sensor that is recognized by any of the Reprint modules.
     */
    NO_HARDWARE,
    /**
     * The sensor is temporarily unavailable, perhaps because the device is locked, or another
     * operation is already pending.
     */
    HARDWARE_UNAVAILABLE,
    /**
     * The user has not registered any fingerprints with the system.
     */
    NO_FINGERPRINTS_REGISTERED,
    /**
     * The sensor was unable to read the fingerprint, perhaps because the finger was moved too
     * quickly, or the sensor was dirty.
     */
    SENSOR_FAILED,
    /**
     * Too many failed attempts have been made, and the user cannot make another attempt for an
     * unspecified amount of time.
     */
    LOCKED_OUT,
    /**
     * The sensor has been running for too long without reading anything.
     * <p/>
     * The amount of time that the sensor can be running before a timeout is system and sensor specific, but
     * is usually around 30 seconds. It is safe to immediately start another authentication attempt.
     */
    TIMEOUT,
    /**
     * A fingerprint was read successfully, but that fingerprint was not registered on the device.
     */
    AUTHENTICATION_FAILED,
    /**
     * The authentication failed for an unknown reason.
     */
    UNKNOWN
}
