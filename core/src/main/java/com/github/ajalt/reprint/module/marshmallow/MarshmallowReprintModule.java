package com.github.ajalt.reprint.module.marshmallow;

import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.ReprintModule;

/**
 * A reprint module that authenticates fingerprint using the marshmallow Imprint API.
 */
public class MarshmallowReprintModule implements ReprintModule {
    public static final int TAG = 1;

    // The following FINGERPRINT constants from FingerprintManager, since that class isn't
    // available pre-marshmallow, and they aren't defined in FingerprintManagerCompat for some
    // reason.

    /**
     * The hardware is unavailable. Try again later.
     */
    public static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;

    /**
     * Error state returned when the sensor was unable to process the current image.
     */
    public static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;

    /**
     * Error state returned when the current request has been running too long. This is intended to
     * prevent programs from waiting for the fingerprint sensor indefinitely. The timeout is
     * platform and sensor-specific, but is generally on the order of 30 seconds.
     */
    public static final int FINGERPRINT_ERROR_TIMEOUT = 3;

    /**
     * Error state returned for operations like enrollment; the operation cannot be completed
     * because there's not enough storage remaining to complete the operation.
     */
    public static final int FINGERPRINT_ERROR_NO_SPACE = 4;

    /**
     * The operation was canceled because the fingerprint sensor is unavailable. For example, this
     * may happen when the user is switched, the device is locked or another pending operation
     * prevents or disables it.
     */
    public static final int FINGERPRINT_ERROR_CANCELED = 5;

    /**
     * The operation was canceled because the API is locked out due to too many attempts.
     */
    public static final int FINGERPRINT_ERROR_LOCKOUT = 7;

    // The following ACQUIRED constants are used with help messages
    /**
     * The image acquired was good.
     */
    public static final int FINGERPRINT_ACQUIRED_GOOD = 0;

    /**
     * Only a partial fingerprint image was detected. During enrollment, the user should be informed
     * on what needs to happen to resolve this problem, e.g. "press firmly on sensor."
     */
    public static final int FINGERPRINT_ACQUIRED_PARTIAL = 1;

    /**
     * The fingerprint image was too noisy to process due to a detected condition (i.e. dry skin) or
     * a possibly dirty sensor (See {@link #FINGERPRINT_ACQUIRED_IMAGER_DIRTY}).
     */
    public static final int FINGERPRINT_ACQUIRED_INSUFFICIENT = 2;

    /**
     * The fingerprint image was too noisy due to suspected or detected dirt on the sensor. For
     * example, it's reasonable return this after multiple {@link #FINGERPRINT_ACQUIRED_INSUFFICIENT}
     * or actual detection of dirt on the sensor (stuck pixels, swaths, etc.). The user is expected
     * to take action to clean the sensor when this is returned.
     */
    public static final int FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3;

    /**
     * The fingerprint image was unreadable due to lack of motion. This is most appropriate for
     * linear array sensors that require a swipe motion.
     */
    public static final int FINGERPRINT_ACQUIRED_TOO_SLOW = 4;

    /**
     * The fingerprint image was incomplete due to quick motion. While mostly appropriate for linear
     * array sensors,  this could also happen if the finger was moved during acquisition. The user
     * should be asked to move the finger slower (linear) or leave the finger on the sensor longer.
     */
    public static final int FINGERPRINT_ACQUIRED_TOO_FAST = 5;

    /**
     * A fingerprint was read that is not registered.
     * <p/>
     * This constant is defined by reprint, and is not in the FingerprintManager.
     */
    public static final int FINGERPRINT_AUTHENTICATION_FAILED = 1001;

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
    public void authenticate(CancellationSignal cancellationSignal, final AuthenticationListener listener) {
        fingerprintManager.authenticate(null, 0, cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                AuthenticationFailureReason failureReason = AuthenticationFailureReason.UNKNOWN;
                switch (errMsgId) {
                    case FINGERPRINT_ERROR_HW_UNAVAILABLE:
                        failureReason = AuthenticationFailureReason.HARDWARE_UNAVAILABLE;
                        break;
                    case FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                    case FINGERPRINT_ERROR_NO_SPACE:
                        failureReason = AuthenticationFailureReason.SENSOR_FAILED;
                        break;
                    case FINGERPRINT_ERROR_CANCELED:
                        failureReason = AuthenticationFailureReason.CANCELLED;
                        break;
                    case FINGERPRINT_ERROR_TIMEOUT:
                        failureReason = AuthenticationFailureReason.TIMEOUT;
                        break;
                    case FINGERPRINT_ERROR_LOCKOUT:
                        failureReason = AuthenticationFailureReason.LOCKED_OUT;
                        break;
                }

                listener.onFailure(failureReason, true, errString, TAG, errMsgId);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                listener.onFailure(AuthenticationFailureReason.SENSOR_FAILED, false, helpString, TAG, helpMsgId);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                listener.onSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, null, TAG, FINGERPRINT_AUTHENTICATION_FAILED);
            }
        }, null);
    }
}
