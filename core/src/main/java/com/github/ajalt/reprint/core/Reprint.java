package com.github.ajalt.reprint.core;

import android.content.Context;

/**
 * Static methods for performing fingerprint authentication.
 * <p/>
 * Call {@link #initialize(Context)} in your application's {@code onCreate}, then call {@link
 * #authenticate(AuthenticationListener)} to perform authentication.
 */
public class Reprint {
    public static final int DEFAULT_RESTART_COUNT = 5;

    /**
     * Load all available reprint modules.
     * <p/>
     * This is equivalent to calling {@link #registerModule(ReprintModule)} with the spass module,
     * if included, followed by the marshmallow module.
     */
    public static void initialize(Context context) {
        ReprintInternal.INSTANCE.initialize(context);
    }

    /**
     * Register an individual spass module.
     * <p/>
     * This is only necessary if you want to customize which modules are loaded, or the order in
     * which they're registered. Most use cases should just call {@link #initialize(Context)}
     * instead.
     * <p/>
     * Registering the same module twice will have no effect. The original module instance will
     * remain registered.
     *
     * @param module The module to register.
     */
    public static void registerModule(ReprintModule module) {
        ReprintInternal.INSTANCE.registerModule(module);
    }

    /**
     * Return true if a reprint module is registered that has a fingerprint reader.
     */
    public static boolean isHardwarePresent() {
        return ReprintInternal.INSTANCE.isHardwarePresent();
    }

    /**
     * Return true if a reprint module is registered that has registered fingerprints.
     */
    public static boolean hasFingerprintRegistered() {
        return ReprintInternal.INSTANCE.hasFingerprintRegistered();
    }

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * Equivalent to calling {@link #authenticate(AuthenticationListener, int)} with a {@code
     * restartCount} of {@link #DEFAULT_RESTART_COUNT}
     *
     * @param listener The listener that will be notified of authentication events.
     */
    public static void authenticate(AuthenticationListener listener) {
        authenticate(listener, DEFAULT_RESTART_COUNT);
    }

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * If {@link #isHardwarePresent()} or {@link #hasFingerprintRegistered()} return false, no
     * authentication will take place, and the listener's {@link AuthenticationListener#onFailure(AuthenticationFailureReason,
     * boolean, CharSequence, int, int)} will immediately be called with the corresponding failure
     * reason. In this case, the error message will be null, fatal will be true, and the other
     * values are unspecified.
     *
     * @param listener     The listener that will be notified of authentication events.
     * @param restartCount If the authentication times out due to inactivity, the request will be
     *                     automatically restarted up to this many times. The listener will not be
     *                     notified about restarts, but will receive a normal timeout failure once
     *                     the number of retries has been exceeded.
     */
    public static void authenticate(AuthenticationListener listener, int restartCount) {
        ReprintInternal.INSTANCE.authenticate(listener, true, restartCount);
    }

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * This variant will not restart the fingerprint reader after any failure, including non-fatal
     * failures.
     *
     * @param listener The listener that will be notified of authentication events.
     */
    public static void authenticateWithoutRestart(AuthenticationListener listener) {
        ReprintInternal.INSTANCE.authenticate(listener, false, 0);
    }

    /**
     * Cancel any active authentication requests.
     * <p/>
     * If no authentication is active, this call has no effect.
     */
    public static void cancelAuthentication() {
        ReprintInternal.INSTANCE.cancelAuthentication();
    }
}
