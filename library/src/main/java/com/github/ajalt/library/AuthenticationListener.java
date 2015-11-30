package com.github.ajalt.library;

import android.support.annotation.Nullable;

public interface AuthenticationListener {
    void onSuccess();

    void onFailure(int fromModule, AuthenticationFailureReason failureReason, int errorCode, @Nullable CharSequence errorMessage);
}
