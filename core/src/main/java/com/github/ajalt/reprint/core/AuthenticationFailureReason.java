package com.github.ajalt.reprint.core;

/**
 * General categories for authentication failures.
 */
public enum AuthenticationFailureReason {
    /**
     * No registered reprint modules have hardware or registered fingerprints.
     */
    NO_SENSOR,
    /**
     * The sensor is temporarily unavailable, perhaps because the device is locked, or another
     * operation is already pending.
     */
    HARDWARE_UNAVAILABLE,
    /**
     * An authentication request was started
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
    LOCKOUT,
    /**
     * The sensor has been running for too long without reading anything.
     * <p/>
     * The timeout period is system and sensor specific, but is usually around 30 seconds. It is
     * safe to immediately start another authentication attempt.
     */
    TIMEOUT,
    /**
     * A fingerprint was read that was not registered on the device.
     */
    AUTHENTICATION_FAILED,
    /**
     * The authentication failed for an unknown reason.
     */
    UNKNOWN
}
