package com.github.ajalt.reprint.core;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;

import com.github.ajalt.reprint.module.marshmallow.MarshmallowReprintModule;

import java.lang.reflect.Constructor;

/**
 * Static methods for performing fingerprint authentication.
 * <p/>
 * Call {@link #initialize(Context)} in your application's {@code onCreate}, then call {@link
 * #authenticate(AuthenticationListener)} to perform authentication.
 */
public enum ReprintInternal {
    INSTANCE;

    @Nullable
    private CancellationSignal cancellationSignal;

    @Nullable
    private ReprintModule module;

    /**
     * Return the global Reprint instance.
     */
    public static ReprintInternal instance() {
        return INSTANCE;
    }

    /**
     * Load all available reprint modules.
     * <p/>
     * This is equivalent to calling {@link #registerModule(ReprintModule)} with the spass module,
     * if included, followed by the marshmallow module.
     */
    public static ReprintInternal initialize(Context context) {
        if (INSTANCE.module != null) return INSTANCE;

        // Load the spass module if it was included.
        try {
            final Class<?> spassModuleClass = Class.forName("com.github.ajalt.reprint.module.spass.SpassReprintModule");
            final Constructor<?> constructor = spassModuleClass.getConstructor(Context.class);
            ReprintModule module = (ReprintModule) constructor.newInstance(context);
            INSTANCE.registerModule(module);
        } catch (Exception ignored) {
        }

        INSTANCE.registerModule(new MarshmallowReprintModule(context));

        return INSTANCE;
    }

    /**
     * Register an individual spass module.
     * <p/>
     * This is only necessary if you want to customize which modules are loaded, or the order in
     * which they're registered. Most use cases should just call {@link #initialize(Context)}
     * instead.
     * <p/>
     * Registering the same module twice will have no effect. The original module instance will
     * remain registered.
     *
     * @param module The module to register.
     */
    public ReprintInternal registerModule(ReprintModule module) {
        if (this.module != null && module.tag() == this.module.tag()) {
            return this;
        }

        if (module.isHardwarePresent()) {
            this.module = module;
        }

        return this;
    }

    /**
     * Return true if a reprint module is registered that has a fingerprint reader.
     */
    public boolean isHardwarePresent() {
        return module != null && module.isHardwarePresent();
    }

    /**
     * Return true if a reprint module is registered that has registered fingerprints.
     */
    public boolean hasFingerprintRegistered() {
        return module != null && module.hasFingerprintRegistered();
    }

    /**
     * Start a fingerprint authentication request.
     *
     * @param listener The listener that will be notified of authentication events.
     */
    public void authenticate(AuthenticationListener listener) {
        if (module == null || !module.isHardwarePresent() || !module.hasFingerprintRegistered()) {
            listener.onFailure(0, AuthenticationFailureReason.NO_HARDWARE, 0, null);
            return;
        }

        cancellationSignal = new CancellationSignal();
        module.authenticate(listener, cancellationSignal);
    }

    /**
     * Cancel any active authentication requests.
     * <p/>
     * If no authentication is active, this call has no effect.
     */
    public void cancelAuthentication() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }
}
