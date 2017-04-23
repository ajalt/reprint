package com.github.ajalt.reprint.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/** A data class exception that holds the results of an authentication request failure. */
public class AuthenticationResult {
    public enum Status {SUCCESS, NONFATAL_FAILURE, FATAL_FAILURE}

    /**
     * The result of the authenticate call.
     * <p>
     * If the status is RECOVERABLE_FAILURE, the fingerprint sensor is still running, and more
     * events will be emitted. Other statuses are terminal events, and the fingerprint sensor will
     * be stopped by the time they are emitted.
     */
    @NonNull
    public final Status status;
    /**
     * The general reason for the failure.
     * <p>
     * Will be null if the authentication succeeded, and non-null otherwise.
     */
    @Nullable
    public final AuthenticationFailureReason failureReason;
    /**
     * An informative string given by the underlying fingerprint sdk that can be displayed in the
     * ui.
     * <p>
     * This string is never null, and will be localized to the current locale.
     */
    @NonNull
    public final CharSequence errorMessage;
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

    public AuthenticationResult(@NonNull Status status, @Nullable AuthenticationFailureReason failureReason,
                                @NonNull CharSequence errorMessage, int fromModule, int errorCode) {
        this.status = status;
        this.failureReason = failureReason;
        this.errorMessage = errorMessage;
        this.fromModule = fromModule;
        this.errorCode = errorCode;
    }
}
