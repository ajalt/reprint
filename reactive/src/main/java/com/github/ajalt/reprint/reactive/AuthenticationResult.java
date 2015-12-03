package com.github.ajalt.reprint.reactive;

import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;

/**
 * A data class that holds the results of an authentication request.
 * <p/>
 * When a fingerprint is authenticated successfully, the failureReason will be null, and the other
 * values will be unspecified.
 * <p/>
 * On a failure, the values are the same as the arguments to {@link com.github.ajalt.reprint.core.AuthenticationListener#onFailure(AuthenticationFailureReason,
 * boolean, CharSequence, int, int)}
 */
public class AuthenticationResult {
    @Nullable
    public final AuthenticationFailureReason failureReason;

    @Nullable
    public final CharSequence errorMessage;

    public final boolean fatal;
    public final int fromModule;
    public final int errorCode;

    public AuthenticationResult(@Nullable AuthenticationFailureReason failureReason, boolean fatal,
                                @Nullable CharSequence errorMessage, int fromModule, int errorCode) {
        this.failureReason = failureReason;
        this.fatal = fatal;
        this.errorMessage = errorMessage;
        this.fromModule = fromModule;
        this.errorCode = errorCode;
    }
}
