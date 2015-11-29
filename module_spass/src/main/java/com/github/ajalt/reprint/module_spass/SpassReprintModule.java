package com.github.ajalt.reprint.module_spass;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;

import com.github.ajalt.library.AuthenticationListener;
import com.github.ajalt.library.ReprintModule;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

public class SpassReprintModule implements ReprintModule {
    @Nullable
    private final Spass spass;

    public SpassReprintModule(Context context) {
        Spass s;
        try {
            s = new Spass();
            s.initialize(context);
        } catch (Exception e) {
            s = null;
        }
        spass = s;
    }

    @Override
    public boolean isHardwarePresent() {
        try {
            return spass != null && spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        } catch (Exception ignored) {
            return false;
        }
    }


    @Override
    public boolean hasFingerprintRegistered() {
        try {
            return isHardwarePresent() && new SpassFingerprint(context).hasRegisteredFinger();
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public void authenticate(AuthenticationListener listener, CancellationSignal cancellationSignal) {

    }

    @Override
    public int tag() {
        return 0;
    }
}
