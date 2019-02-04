package com.github.ajalt.reprint.module.pie;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.github.ajalt.library.R;
import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.ReprintModule;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.os.CancellationSignal;

@TargetApi(Build.VERSION_CODES.P)
@RequiresApi(Build.VERSION_CODES.P)
public final class PieReprintModule implements ReprintModule {
    private static final int TAG = 2;

    // The following FINGERPRINT constants are copied from FingerprintManager, since that class
    // isn't available pre-marshmallow, and they aren't defined in FingerprintManagerCompat for some
    // reason.

    /**
     * The hardware is unavailable. Try again later.
     */
    private static final int FINGERPRINT_ERROR_HW_UNAVAILABLE = 1;

    /**
     * Error state returned when the sensor was unable to process the current image.
     */
    private static final int FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2;

    /**
     * Error state returned when the current request has been running too long. This is intended to
     * prevent programs from waiting for the fingerprint sensor indefinitely. The timeout is
     * platform and sensor-specific, but is generally on the order of 30 seconds.
     */
    private static final int FINGERPRINT_ERROR_TIMEOUT = 3;

    /**
     * Error state returned for operations like enrollment; the operation cannot be completed
     * because there's not enough storage remaining to complete the operation.
     */
    private static final int FINGERPRINT_ERROR_NO_SPACE = 4;

    /**
     * The operation was canceled because the fingerprint sensor is unavailable. For example, this
     * may happen when the user is switched, the device is locked or another pending operation
     * prevents or disables it.
     */
    private static final int FINGERPRINT_ERROR_CANCELED = 5;

    /**
     * The operation was canceled because the API is locked out due to too many attempts.
     */
    private static final int FINGERPRINT_ERROR_LOCKOUT = 7;

    /**
     * A fingerprint was read that is not registered.
     * <p/>
     * This constant is defined by reprint, and is not in the FingerprintManager.
     */
    private static final int FINGERPRINT_AUTHENTICATION_FAILED = 1001;

    private final Context context;
    private final Reprint.Logger logger;

    public PieReprintModule(Context context, Reprint.Logger logger) {
        this.context = context.getApplicationContext();
        this.logger = logger;
    }

    @Nullable
    private BiometricPrompt getBiometricPromptService() {
        try {
            return context.getSystemService(BiometricPrompt.class);
        } catch (Exception e) {
            logger.logException(e, "Could not get biometric prompt system service on API that should support it.");
        } catch (NoClassDefFoundError e) {
            logger.log("BiometricPrompt not available on this device");
        }
        return null;
    }

    @Override
    public int tag() {
        return TAG;
    }

    @Override
    public boolean isHardwarePresent() {
        final BiometricPrompt biometricPromptService = getBiometricPromptService();
        if (biometricPromptService == null) return false;
        try {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT);
        } catch (SecurityException | NullPointerException e) {
            logger.logException(e, "PieReprintModule: isHardwarePresent failed unexpectedly");
            return false;
        }
    }

    /**
     * See https://stackoverflow.com/a/53973970/3667225
     */
    @Override
    public boolean hasFingerprintRegistered() throws SecurityException {
        final BiometricPrompt biometricPromptService = getBiometricPromptService();
        if (biometricPromptService == null) return false;
        // Some devices with fingerprint sensors throw an IllegalStateException when trying to parse an
        // internal settings file during this call. See #29.
        try {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
            } catch (Exception e) {
                return false;
            }

            KeyGenerator keyGenerator;
            try {
                keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            } catch (NoSuchAlgorithmException |
                NoSuchProviderException e) {
                return false;
            }

            if (keyGenerator == null || keyStore == null) {
                return false;
            }

            try {
                keyStore.load(null);
                keyGenerator.init(new
                    KeyGenParameterSpec.Builder("dummy_key",
                    KeyProperties.PURPOSE_ENCRYPT |
                        KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
                return false;
            }
            return true;
        } catch (IllegalStateException e) {
            logger.logException(e, "PieReprintModule: hasFingerprintRegistered failed unexpectedly");
            return false;
        }
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
                              final int restartCount) throws SecurityException {
        final BiometricPrompt biometricPromptService = getBiometricPromptService();

        if (biometricPromptService == null) {
            listener.onFailure(AuthenticationFailureReason.UNKNOWN, true,
                context.getString(R.string.fingerprint_error_hw_not_available), TAG, FINGERPRINT_ERROR_CANCELED);
            return;
        }

        final BiometricPrompt.AuthenticationCallback callback =
            new AuthCallback(restartCount, restartPredicate, cancellationSignal, listener);

        // Why getCancellationSignalObject returns an Object is unexplained
        final android.os.CancellationSignal signalObject = cancellationSignal == null ? null :
            (android.os.CancellationSignal) cancellationSignal.getCancellationSignalObject();

        // Occasionally, an NPE will bubble up out of BiometricPrompt#authenticate
        try {
            biometricPromptService.authenticate(signalObject, context.getMainExecutor(), callback);
        } catch (NullPointerException e) {
            logger.logException(e, "PieReprintModule: authenticate failed unexpectedly");
            listener.onFailure(AuthenticationFailureReason.UNKNOWN, true,
                context.getString(R.string.fingerprint_error_unable_to_process), TAG, FINGERPRINT_ERROR_CANCELED);
        }
    }

    final class AuthCallback extends BiometricPrompt.AuthenticationCallback {
        private final Reprint.RestartPredicate restartPredicate;
        private final CancellationSignal cancellationSignal;
        private final AuthenticationListener listener;
        private int restartCount;

        private AuthCallback(int restartCount, Reprint.RestartPredicate restartPredicate,
                             CancellationSignal cancellationSignal, AuthenticationListener listener) {
            this.restartCount = restartCount;
            this.restartPredicate = restartPredicate;
            this.cancellationSignal = cancellationSignal;
            this.listener = listener;
        }

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
                case FINGERPRINT_ERROR_TIMEOUT:
                    failureReason = AuthenticationFailureReason.TIMEOUT;
                    break;
                case FINGERPRINT_ERROR_LOCKOUT:
                    failureReason = AuthenticationFailureReason.LOCKED_OUT;
                    break;
                case FINGERPRINT_ERROR_CANCELED:
                    // Don't send a cancelled message.
                    return;
            }

            if (errMsgId == FINGERPRINT_ERROR_TIMEOUT && restartPredicate.invoke(failureReason, restartCount)) {
                authenticate(cancellationSignal, listener, restartPredicate, restartCount);
            } else {
                listener.onFailure(failureReason, true, errString, TAG, errMsgId);
            }
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            if (!restartPredicate.invoke(AuthenticationFailureReason.SENSOR_FAILED, restartCount++)) {
                cancellationSignal.cancel();
            }
            listener.onFailure(AuthenticationFailureReason.SENSOR_FAILED, false, helpString, TAG, helpMsgId);
        }

        @Override
        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
            listener.onSuccess(TAG);
        }

        @Override
        public void onAuthenticationFailed() {
            listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, false,
                context.getString(R.string.fingerprint_not_recognized), TAG, FINGERPRINT_AUTHENTICATION_FAILED);
        }
    }
}
