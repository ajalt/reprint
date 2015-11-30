package com.github.ajalt.library;

import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

public class MarshmallowReprintModule implements ReprintModule {
    public static final int TAG = 1;
    private final FingerprintManagerCompat fingerprintManager;

    public MarshmallowReprintModule(Context context) {
        fingerprintManager = FingerprintManagerCompat.from(context.getApplicationContext());
    }

    @Override
    public int tag() {
        return TAG;
    }

    @Override
    public boolean isHardwarePresent() {
        return fingerprintManager.isHardwareDetected();
    }

    @Override
    public boolean hasFingerprintRegistered() {
        return fingerprintManager.hasEnrolledFingerprints();
    }

    @Override
    public void authenticate(final AuthenticationListener listener, CancellationSignal cancellationSignal) {
        fingerprintManager.authenticate(null, 0, cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                listener.onFailure();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                listener.onFailure();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                listener.onSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                listener.onFailure();
            }
        }, null);
    }
}
