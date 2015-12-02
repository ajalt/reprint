package com.github.ajalt.reprint.core;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;

import com.github.ajalt.reprint.module.marshmallow.MarshmallowReprintModule;

import java.lang.reflect.Constructor;

public enum Reprint {
    INSTANCE;

    @Nullable
    private CancellationSignal cancellationSignal;

    @Nullable
    private ReprintModule module;

    public static Reprint instance() {
        return INSTANCE;
    }

    public static Reprint initialize(Context context) {
        if (INSTANCE.module != null) return INSTANCE;
        
        // Load the spass module if it was included.
        try {
            final Class<?> spassModuleClass = Class.forName("com.github.ajalt.reprint.module.spass.SpassReprintModule");
            final Constructor<?> constructor = spassModuleClass.getConstructor(Context.class);
            ReprintModule module = (ReprintModule) constructor.newInstance(context);
            INSTANCE.registerModule(module);
        } catch (Exception ignored) {}

        INSTANCE.registerModule(new MarshmallowReprintModule(context));

        return INSTANCE;
    }

    public Reprint registerModule(ReprintModule module) {
        if (this.module != null && module.tag() == this.module.tag()) {
            return this;
        }

        if (module.isHardwarePresent()) {
            this.module = module;
        }

        return this;
    }

    public boolean isHardwarePresent() {
        return module != null && module.isHardwarePresent();
    }

    public boolean hasFingerprintRegistered() {
        return module != null && module.hasFingerprintRegistered();
    }

    public void authenticate(AuthenticationListener listener) {
        if (module == null || !module.isHardwarePresent() || !module.hasFingerprintRegistered()) {
            listener.onFailure(0, AuthenticationFailureReason.NO_SENSOR, 0, null);
            return;
        }

        cancellationSignal = new CancellationSignal();
        module.authenticate(listener, cancellationSignal);
    }

    public void cancelAuthentication() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }
}
