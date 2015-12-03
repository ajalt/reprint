package com.github.ajalt.reprint.core;

import android.support.v4.os.CancellationSignal;

/**
 * A reprint module handles communication with a specific fingerprint api.
 * <p/>
 * Implement this interface to add a new api to Reprint, then pass an instance of this interface to
 * {@link Reprint#registerModule(ReprintModule)}
 */
public interface ReprintModule {
    /**
     * Return true if a fingerprint reader of this type exists on the current device.
     * <p/>
     * Don't call this method directly. Register an instance of this module with Reprint, then call
     * {@link Reprint#isHardwarePresent()}
     */
    boolean isHardwarePresent();

    /**
     * Return true if there are registered fingerprints on the current device.
     * <p/>
     * If this returns true, {@link #authenticate(CancellationSignal, AuthenticationListener)}, it
     * should be possible to perform authentication with this module.
     * <p/>
     * Don't call this method directly. Register an instance of this module with Reprint, then call
     * {@link Reprint#hasFingerprintRegistered()}
     */
    boolean hasFingerprintRegistered();

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * Don't call this method directly. Register an instance of this module with Reprint, then call
     * {@link Reprint#authenticate(AuthenticationListener)}
     *
     * @param cancellationSignal A signal that can cancel the authentication request.
     * @param listener           A listener that will be notified of the authentication status.
     */
    void authenticate(CancellationSignal cancellationSignal, AuthenticationListener listener);

    /**
     * A tag uniquely identifying this class. It must be the same for all instances of each class,
     * and each class's tag must be unique among registered modules.
     */
    int tag();
}
