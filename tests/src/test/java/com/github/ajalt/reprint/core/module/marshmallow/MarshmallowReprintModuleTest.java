package com.github.ajalt.reprint.core.module.marshmallow;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.module.marshmallow.MarshmallowReprintModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressWarnings("MissingPermission")
@RunWith(MockitoJUnitRunner.class)
public class MarshmallowReprintModuleTest {
    private static final Reprint.Logger LOGGER = new Reprint.Logger() {
        public void log(String message) {
            System.out.println(message);
        }

        public void logException(Throwable throwable, String message) {
            System.out.println(message);
            System.out.printf("%s %s%n", throwable.toString(), throwable.getMessage());
        }
    };

    public MarshmallowReprintModule module;
    @Mock public Context context;
    @Mock public FingerprintManager fingerprintManager;

    @Before
    public void setup() {
        when(context.getApplicationContext()).thenReturn(context);
        module = new MarshmallowReprintModule(context, LOGGER);
    }

    private void setupValidManager() {
        setupManager(this.fingerprintManager);
        when(fingerprintManager.isHardwareDetected()).thenReturn(true);
        when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(true);
    }

    private void setupManager(@Nullable FingerprintManager fingerprintManager) {
        when(context.getSystemService(any(Class.class))).thenReturn(fingerprintManager);
    }

    @Test
    public void fingerprintManager_null() throws Exception {
        setupManager(null);
        assertThat(module.isHardwarePresent()).isFalse();
        assertThat(module.hasFingerprintRegistered()).isFalse();
    }

    @Test
    public void fingerprintManager_noHardware() throws Exception {
        setupManager(fingerprintManager);
        when(fingerprintManager.isHardwareDetected()).thenReturn(false);
        when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(false);
        assertThat(module.isHardwarePresent()).isFalse();
        assertThat(module.hasFingerprintRegistered()).isFalse();
    }

    @Test
    public void fingerprintManager_noFingerprints() throws Exception {
        setupManager(fingerprintManager);
        when(fingerprintManager.isHardwareDetected()).thenReturn(true);
        when(fingerprintManager.hasEnrolledFingerprints()).thenReturn(false);
        assertThat(module.isHardwarePresent()).isTrue();
        assertThat(module.hasFingerprintRegistered()).isFalse();
    }

    /** Issue #29 */
    @Test
    public void fingerprintManager_enrolledFingerprintError() throws Exception {
        setupManager(fingerprintManager);
        when(fingerprintManager.isHardwareDetected()).thenReturn(true);
        when(fingerprintManager.hasEnrolledFingerprints()).thenThrow(
                new IllegalStateException(
                        "Failed parsing settings file: /data/system/users/0/settings_fingerprint.xml")
        );
        assertThat(module.isHardwarePresent()).isTrue();
        assertThat(module.hasFingerprintRegistered()).isFalse();
    }

    @Test
    public void fingerprintManager_fingerprints() throws Exception {
        setupValidManager();
        assertThat(module.isHardwarePresent()).isTrue();
        assertThat(module.hasFingerprintRegistered()).isTrue();
    }
}
