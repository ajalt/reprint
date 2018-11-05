package com.github.ajalt.reprint.module.spass;

import android.content.Context;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.ReprintModule;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import androidx.annotation.StringRes;
import androidx.core.os.CancellationSignal;

import static com.github.ajalt.reprint.core.AuthenticationFailureReason.TIMEOUT;

/**
 * A Reprint module that authenticates fingerprints using the Samsung Pass SDK.
 * <p>
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
    private final Spass spass;
    private final Reprint.Logger logger;
    private SpassFingerprint spassFingerprint;

    @SuppressWarnings("unused") // Call via reflection
    public SpassReprintModule(Context context, Reprint.Logger logger) {
        this.context = context.getApplicationContext();
        this.logger = logger;

        Spass s;
        try {
            s = new Spass();
            s.initialize(this.context);
        } catch (SecurityException e) {
            // Rethrow security exceptions, which happen when the manifest permission is missing.
            throw e;
        } catch (Exception ignored) {
            // The awful spass sdk throws an exception on non-samsung devices, so swallow it here.
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
        } catch (Exception ignored) {
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
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void authenticate(final CancellationSignal cancellationSignal,
                             final AuthenticationListener listener,
                             final Reprint.RestartPredicate restartPredicate) {
        authenticate(cancellationSignal, listener, restartPredicate, 0);
    }

    private void authenticate(final CancellationSignal cancellationSignal,
                             final AuthenticationListener listener,
                             final Reprint.RestartPredicate restartPredicate,
                             final int restartCount) {
        if (spassFingerprint == null) {
            spassFingerprint = new SpassFingerprint(context);
        }
        try {
            if (!spassFingerprint.hasRegisteredFinger()) {
                listener.onFailure(AuthenticationFailureReason.NO_FINGERPRINTS_REGISTERED, true,
                        context.getString(R.string.fingerprint_error_hw_not_available), TAG, STATUS_NO_REGISTERED_FINGERPRINTS);
                return;
            }
        } catch (Throwable ignored) {
            listener.onFailure(AuthenticationFailureReason.HARDWARE_UNAVAILABLE, true,
                    context.getString(R.string.fingerprint_error_hw_not_available), TAG, STATUS_HW_UNAVAILABLE);
            return;
        }

        cancelFingerprintRequest(spassFingerprint);

        try {
            spassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener() {

                @Override
                public void onCompleted() {
                }

                @Override
                public void onFinished(int eventStatus) {
                    switch (eventStatus) {
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                            listener.onSuccess(TAG);
                            return;
                        case SpassFingerprint.STATUS_QUALITY_FAILED:
                            fail(AuthenticationFailureReason.SENSOR_FAILED, false, R.string.fingerprint_acquired_partial, eventStatus);
                            break;
                        case SpassFingerprint.STATUS_SENSOR_FAILED:
                            fail(AuthenticationFailureReason.SENSOR_FAILED, false, R.string.fingerprint_acquired_insufficient, eventStatus);
                            break;
                        case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
                            fail(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, R.string.fingerprint_not_recognized, eventStatus);
                            break;
                        case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                            fail(TIMEOUT, true, R.string.fingerprint_error_timeout, eventStatus);
                            break;
                        default:
                            fail(AuthenticationFailureReason.UNKNOWN, true, R.string.fingerprint_error_hw_not_available, eventStatus);
                            break;
                        case SpassFingerprint.STATUS_USER_CANCELLED:
                            // Don't send a cancelled message.
                            break;
                    }
                }

                private void fail(AuthenticationFailureReason reason, boolean fatal, @StringRes int message, int status) {
                    fail(reason, fatal, context.getString(message), status);
                }

                private void fail(AuthenticationFailureReason reason, boolean fatal, String message, int status) {
                    listener.onFailure(reason, fatal, message, TAG, status);
                    if ((!fatal || reason == TIMEOUT) && restartPredicate.invoke(reason, restartCount)) {
                        authenticate(cancellationSignal, listener, restartPredicate, restartCount + 1);
                    }
                }

                @Override
                public void onReady() {}

                @Override
                public void onStarted() {}
            });
        } catch (Throwable t) {
            logger.logException(t, "SpassReprintModule: fingerprint identification would not start");
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
