package com.github.ajalt.reprint.reactive;

import android.support.v4.os.CancellationSignal;

import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.ReprintModule;

import java.util.Random;

public class TestReprintModule implements ReprintModule {
    public final int TAG = new Random().nextInt(); // Register a new module each test
    public CancellationSignal cancellationSignal;
    public AuthenticationListener listener;
    public boolean restartOnNonFatal;

    @Override public boolean isHardwarePresent() {
        return true;
    }

    @Override public boolean hasFingerprintRegistered() {
        return true;
    }

    @Override
    public void authenticate(CancellationSignal cancellationSignal, AuthenticationListener listener, boolean restartOnNonFatal) {
        this.cancellationSignal = cancellationSignal;
        this.listener = listener;
        this.restartOnNonFatal = restartOnNonFatal;
    }

    @Override public int tag() {
        return TAG;
    }
}
