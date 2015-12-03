package com.github.ajalt.reprint.reactive;

import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;

public class AuthenticationResult {
    @Nullable
    public final AuthenticationFailureReason failureReason;

    @Nullable
    public final CharSequence errorMessage;

    public final int fromModule;
    public final int errorCode;

    public AuthenticationResult(@Nullable AuthenticationFailureReason failureReason,
                                @Nullable CharSequence errorMessage, int fromModule, int errorCode) {
        this.failureReason = failureReason;
        this.errorMessage = errorMessage;
        this.fromModule = fromModule;
        this.errorCode = errorCode;
    }
}
