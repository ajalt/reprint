package com.github.ajalt.library;

import android.support.v4.os.CancellationSignal;

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

        throw new RuntimeException("No registered modules have fingerprints available.");
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
