package com.github.ajalt.reprint.core;

import android.support.annotation.Nullable;

public interface AuthenticationListener {
    void onSuccess();

    void onFailure(int fromModule, AuthenticationFailureReason failureReason,
                   boolean fatal, int errorCode, @Nullable CharSequence errorMessage);
}
