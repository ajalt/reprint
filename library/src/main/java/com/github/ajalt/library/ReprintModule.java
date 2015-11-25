package com.github.ajalt.library;

import android.support.v4.os.CancellationSignal;

public interface ReprintModule {
    boolean isHardwarePresent();

    boolean hasFingerprintRegistered();

    void authenticate(AuthenticationListener listener, CancellationSignal cancellationSignal);

    int tag();
}
