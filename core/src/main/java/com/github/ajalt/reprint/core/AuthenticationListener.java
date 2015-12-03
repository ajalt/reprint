package com.github.ajalt.reprint.core;

import android.support.annotation.Nullable;

public interface AuthenticationListener {
    void onSuccess();

    void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                   @Nullable CharSequence errorMessage, int fromModule,
                   int errorCode);
}
