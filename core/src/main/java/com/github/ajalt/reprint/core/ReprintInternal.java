package com.github.ajalt.reprint.core;

import android.content.Context;
import android.os.Build;
import android.support.v4.os.CancellationSignal;

import com.github.ajalt.library.R;
import com.github.ajalt.reprint.module.marshmallow.MarshmallowReprintModule;

import java.lang.reflect.Constructor;

/**
 * Methods for performing fingerprint authentication.
 *
 * @hide
 */
enum ReprintInternal {
    INSTANCE;

    public static final Reprint.Logger NULL_LOGGER = new Reprint.Logger() {
        public void log(String message) {}

        public void logException(Throwable throwable, String message) {}
    };

    private static final String REPRINT_SPASS_MODULE = "com.github.ajalt.reprint.module.spass.SpassReprintModule";
    private CancellationSignal cancellationSignal;
    private ReprintModule module;
    private Context context;

    public ReprintInternal initialize(Context context, Reprint.Logger logger) {
        this.context = context.getApplicationContext();

        // The SPass module doesn't work below API 17, and the Imprint module obviously requires
        // Marshmallow.
        if (module != null || Build.VERSION.SDK_INT < 17) return this;

        if (logger == null) logger = ReprintInternal.NULL_LOGGER;

        // Load the spass module if it was included.
        try {
            final Class<?> spassModuleClass = Class.forName(REPRINT_SPASS_MODULE);
            final Constructor<?> constructor = spassModuleClass.getConstructor(Context.class);
            ReprintModule module = (ReprintModule) constructor.newInstance(context, logger);
            registerModule(module);
            return this;
        } catch (Exception ignored) {
        }

        registerModule(new MarshmallowReprintModule(context, logger));

        return this;
    }

    public ReprintInternal registerModule(ReprintModule module) {
        if (module == null || this.module != null && module.tag() == this.module.tag()) {
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

    /**
     * Start an authentication request.
     *
     * @param listener          The listener to be notified.
     * @param restartOnNonFatal If true, restartCount is ignored and only one listener callback will
     *                          ever be called.
     * @param restartCount      If restartOnNonFatal is false, this is the number of times to
     *                          restart on a timeout. Other nonfatal errors will be restarted
     *                          indefinitely.
     */
    public void authenticate(final AuthenticationListener listener, boolean restartOnNonFatal, int restartCount) {
        if (module == null || !module.isHardwarePresent()) {
            listener.onFailure(AuthenticationFailureReason.NO_HARDWARE, true,
                    getString(R.string.fingerprint_error_hw_not_available), 0, 0);
            return;
        }

        if (!module.hasFingerprintRegistered()) {
            listener.onFailure(AuthenticationFailureReason.NO_FINGERPRINTS_REGISTERED, true,
                    getString(R.string.fingerprint_not_recognized), 0, 0);
            return;
        }

        cancellationSignal = new CancellationSignal();
        if (restartOnNonFatal) {
            module.authenticate(cancellationSignal, restartingListener(listener, restartCount), true);
        } else {
            module.authenticate(cancellationSignal, listener, false);
        }
    }

    public void cancelAuthentication() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    private String getString(int resid) {
        return context == null ? null : context.getString(resid);
    }

    private AuthenticationListener restartingListener(final AuthenticationListener originalListener, final int restartCount) {
        return new AuthenticationListener() {
            @Override
            public void onSuccess(int moduleTag) {
                originalListener.onSuccess(moduleTag);
            }

            @Override
            public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                if (module != null && cancellationSignal != null &&
                        failureReason == AuthenticationFailureReason.TIMEOUT && restartCount > 0) {
                    module.authenticate(cancellationSignal, restartingListener(originalListener, restartCount - 1), true);
                } else {
                    originalListener.onFailure(failureReason, fatal, errorMessage, moduleTag, errorCode);
                }
            }
        };
    }
}
