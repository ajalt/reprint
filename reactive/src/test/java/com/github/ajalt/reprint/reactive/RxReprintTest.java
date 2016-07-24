package com.github.ajalt.reprint.reactive;

import android.support.v4.os.CancellationSignal;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.ReprintModule;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import rx.observers.TestSubscriber;
public class RxReprintTest {
    public TestModule module;
    public TestSubscriber<Integer> ts;

    @Before
    public void setup() {
        module = new TestModule();
        module.listener = null;
        module.cancellationSignal = null;
        Reprint.registerModule(module);

        ts = TestSubscriber.create(0L);

        RxReprint.authenticate().subscribe(ts);
        assert module.listener != null;
    }

    @Test
    public void successfulRequest() throws Exception {
        ts.requestMore(1);
        module.listener.onSuccess(module.TAG);
        ts.assertValue(module.TAG);
        ts.assertCompleted();
    }

    @Test
    public void successfulRequestBackpressure() throws Exception {
        module.listener.onSuccess(module.TAG);
        ts.assertNoValues();
        ts.requestMore(1);
        ts.assertValue(module.TAG);
        ts.assertCompleted();
    }

    @Test
    public void failedRequest() throws Exception {
        ts.requestMore(1);
        module.listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }

    public static class TestModule implements ReprintModule {
        public final int TAG = new Random().nextInt(); // Register a new module each test
        public CancellationSignal cancellationSignal;
        public AuthenticationListener listener;
        public boolean restartOnNonFatal;

        @Override public boolean isHardwarePresent() {
            return true;
        }

        @Override public boolean hasFingerprintRegistered() {
            return true;
        }

        @Override
        public void authenticate(CancellationSignal cancellationSignal, AuthenticationListener listener, boolean restartOnNonFatal) {
            this.cancellationSignal = cancellationSignal;
            this.listener = listener;
            this.restartOnNonFatal = restartOnNonFatal;
        }

        @Override public int tag() {
            return TAG;
        }
    }

}
