package com.github.ajalt.library;

import android.support.v4.os.CancellationSignal;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public enum Reprint {
    INSTANCE;

    public static Reprint instance() {
        return INSTANCE;
    }

    public Reprint registerModule(ReprintModule module) {
        for (ReprintModule existing : modules) {
            if (module.tag() == existing.tag()) {
                throw new IllegalArgumentException("Cannot register the same module type twice");
            }
        }
        modules.add(module);
        return this;
    }

    public boolean isHardwarePresent() {
        for (ReprintModule module : modules) {
            Log.d("Reprint", "module: " + module + " " + module.isHardwarePresent());
            if (module.isHardwarePresent()) return true;
        }
        return false;
    }

    public boolean hasFingerprintRegistered() {
        for (ReprintModule module : modules) {
            if (module.hasFingerprintRegistered()) return true;
        }
        return false;
    }

    public void authenticate(AuthenticationListener listener) {
        if (modules.isEmpty()) {
            throw new RuntimeException("Must register a reprint module before calling authenticate");
        }

        for (ReprintModule module : modules) {
            if (module.isHardwarePresent() && module.hasFingerprintRegistered()) {
                cancellationSignal = new CancellationSignal();
                module.authenticate(listener, cancellationSignal);
                return;
            }
        }

        listener.onFailure(0, 0, null);
    }

    public void cancelAuthentication() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    private CancellationSignal cancellationSignal;
    private final List<ReprintModule> modules = new ArrayList<>(2);
}
