package com.github.ajalt.library;

import android.support.annotation.Nullable;

public interface AuthenticationListener {
    void onSuccess();

    void onFailure(int fromModule, int errorCode, @Nullable CharSequence errorMessage);
}
