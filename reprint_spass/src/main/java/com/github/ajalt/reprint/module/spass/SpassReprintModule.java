package com.github.ajalt.reprint.module.spass;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.ReprintModule;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * A Reprint module that authenticates fingerprints using the Samsung Pass SDK.
 * <p/>
 * This module supports all Samsung phones with fingerprint sensors.
 */
public class SpassReprintModule implements ReprintModule {
    public static final int TAG = 2;

    /**
     * A fingerprint was read successfully.
     */
    public static final int STATUS_AUTHENTICATION_SUCCESS = SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS;

    /**
     * The sensor has been running too long.
     */
    public static final int STATUS_TIMEOUT_FAILED = SpassFingerprint.STATUS_TIMEOUT_FAILED;

    /**
     * The sensor was unable to read the finger.
     */
    public static final int STATUS_SENSOR_FAILED = SpassFingerprint.STATUS_SENSOR_FAILED;

    /**
     * The reader was unable to determine the finger.
     */
    public static final int STATUS_QUALITY_FAILED = SpassFingerprint.STATUS_QUALITY_FAILED;


    /**
     * A fingerprint was read that is not registered.
     */
    public static final int STATUS_AUTHENTICATION_FAILED = SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED;

    /**
     * A request was manually cancelled.
     */
    public static final int STATUS_USER_CANCELLED = SpassFingerprint.STATUS_USER_CANCELLED;

    /**
     * An authentication attempt was started without any fingerprints being registered.
     */
    public static final int STATUS_NO_REGISTERED_FINGERPRINTS = 1001;

    /**
     * There was an error in the fingerprint reader hardware.
     */
    public static final int STATUS_HW_UNAVAILABLE = 1002;

    /**
     * The hardware is temporarily locked out due to too many failed attempts.
     */
    public static final int STATUS_LOCKED_OUT = 1003;

    private final Context context;

    @Nullable
    private final Spass spass;

    @Nullable
    private SpassFingerprint spassFingerprint;

    public SpassReprintModule(Context context) {
        this.context = context.getApplicationContext();

        Spass s;
        try {
            s = new Spass();
            s.initialize(this.context);
        } catch (SecurityException e) {
            // Rethrow security exceptions, which happen when the manifest permission is missing.
            throw e;
        } catch (Exception e) {
            // The awful spass sdk throws an exception on non-samsung devices, so swallow it here.
            if (BuildConfig.DEBUG) Log.e("SpassReprintModule", "cannot initialize spass", e);
            s = null;
        }
        spass = s;
    }

    @Override
    public int tag() {
        return TAG;
    }

    @Override
    public boolean isHardwarePresent() {
        try {
            return spass != null && spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("SpassReprintModule",
                    "hasFingerprintRegistered failed", e);
            return false;
        }
    }


    @Override
    public boolean hasFingerprintRegistered() {
        try {
            if (isHardwarePresent()) {
                if (spassFingerprint == null) {
                    spassFingerprint = new SpassFingerprint(context);
                }
                return spassFingerprint.hasRegisteredFinger();
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("SpassReprintModule",
                    "hasFingerprintRegistered failed", e);
        }
        return false;
    }

    @Override
    public void authenticate(final CancellationSignal cancellationSignal, final AuthenticationListener listener, final boolean restartOnNonFatal) {
        if (spassFingerprint == null) {
            spassFingerprint = new SpassFingerprint(context);
        }
        try {
            if (!spassFingerprint.hasRegisteredFinger()) {
                listener.onFailure(AuthenticationFailureReason.NO_FINGERPRINTS_REGISTERED, true, null, TAG, STATUS_NO_REGISTERED_FINGERPRINTS);
                return;
            }
        } catch (Throwable ignored) {
            listener.onFailure(AuthenticationFailureReason.HARDWARE_UNAVAILABLE, true, null, TAG, STATUS_HW_UNAVAILABLE);
            return;
        }

        cancelFingerprintRequest(spassFingerprint);

        try {
            spassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener() {
                @Override
                public void onFinished(int status) {
                    if (BuildConfig.DEBUG) Log.d("SpassReprintModule",
                            "Fingerprint event status: " + status);
                    switch (status) {
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                            listener.onSuccess();
                            return;
                        case SpassFingerprint.STATUS_QUALITY_FAILED:
                        case SpassFingerprint.STATUS_SENSOR_FAILED:
                            listener.onFailure(AuthenticationFailureReason.SENSOR_FAILED, false, null, TAG, status);
                            if (restartOnNonFatal) authenticate(cancellationSignal, listener, true);
                            break;
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
                            listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, null, TAG, status);
                            if (restartOnNonFatal) authenticate(cancellationSignal, listener, true);
                            break;
                        case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                            listener.onFailure(AuthenticationFailureReason.TIMEOUT, true, null, TAG, status);
                            break;
                        default:
                            listener.onFailure(AuthenticationFailureReason.UNKNOWN, true, null, TAG, status);
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED:
                            // Don't send a cancelled message.
                            break;
                    }
                }

                @Override
                public void onReady() {}

                @Override
                public void onStarted() {}
            });
        } catch (Throwable t) {
            if (BuildConfig.DEBUG) Log.e("SpassReprintModule",
                    "fingerprint identification would not start", t);
            listener.onFailure(AuthenticationFailureReason.LOCKED_OUT, true, null, TAG, STATUS_LOCKED_OUT);
            return;
        }

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                cancelFingerprintRequest(spassFingerprint);
            }
        });
    }

    private static void cancelFingerprintRequest(SpassFingerprint spassFingerprint) {
        try {
            spassFingerprint.cancelIdentify();
        } catch (Throwable t) {
            // There's no way to query if there's an active identify request,
            // so just try to cancel and ignore any exceptions.
        }
    }
}
