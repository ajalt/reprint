package com.github.ajalt.reprint.reactive;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.ReprintModule;

/**
 * A data class exception that holds the results of an authentication request failure.
 */
public class AuthenticationFailure extends Exception {
    /**
     * The general reason for the failure.
     */
    public final AuthenticationFailureReason failureReason;
    /**
     * An informative string given by the underlying fingerprint sdk that can be displayed in the
     * ui. This string is never null, and will be localized to the current locale.
     */
    public final CharSequence errorMessage;
    /**
     * If true, the failure is unrecoverable and the sensor cannot be restarted. If false, you can
     * immediately resubscribe to restart the sensor.
     */
    public final boolean fatal;
    /**
     * The {@link ReprintModule#tag()} of the module that is currently active. This is useful to
     * know the meaning of the error code.
     */
    public final int fromModule;
    /**
     * The specific error code returned by the module's underlying sdk. Check the constants defined
     * in the module for possible values and their meanings.
     */
    public final int errorCode;

    public AuthenticationFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                 CharSequence errorMessage, int fromModule, int errorCode) {
        this.failureReason = failureReason;
        this.fatal = fatal;
        this.errorMessage = errorMessage;
        this.fromModule = fromModule;
        this.errorCode = errorCode;
    }
}
