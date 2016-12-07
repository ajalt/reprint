package com.github.ajalt.reprint.reactive;

import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.testing.TestReprintModule;

import org.junit.Before;
import org.junit.Test;

import rx.functions.Func2;
import rx.observers.TestSubscriber;

import static com.github.ajalt.reprint.core.AuthenticationFailureReason.AUTHENTICATION_FAILED;
import static com.github.ajalt.reprint.core.AuthenticationFailureReason.HARDWARE_UNAVAILABLE;
import static com.github.ajalt.reprint.core.AuthenticationFailureReason.SENSOR_FAILED;
import static com.github.ajalt.reprint.core.AuthenticationFailureReason.TIMEOUT;
import static org.junit.Assert.assertNotNull;

public class RxReprintRetryTest {
    public TestReprintModule module;
    public TestSubscriber<Integer> ts;

    @Before
    public void setup() {
        module = new TestReprintModule();
        module.listener = null;
        module.cancellationSignal = null;
        Reprint.registerModule(module);
        ts = TestSubscriber.create();
    }

    public void subscribe(Func2<Integer, Throwable, Boolean> predicate) {
        RxReprint.authenticate().retry(predicate).subscribe(ts);
        assertNotNull(module.listener);
    }

    @Test
    public void retryNonFatal_failsFatal() throws Exception {
        subscribe(RxReprint.retryNonFatal(3));
        module.listener.onFailure(HARDWARE_UNAVAILABLE, true, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }

    @Test
    public void retryNonFatal_passesUnlimitedNonFatal() throws Exception {
        subscribe(RxReprint.retryNonFatal(3));
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoValues();
        ts.assertNoTerminalEvent();
        module.listener.onSuccess(module.TAG);
        ts.assertValue(module.TAG);
        ts.assertCompleted();
    }

    @Test
    public void retryNonFatal_passesLimitedTimeout() throws Exception {
        subscribe(RxReprint.retryNonFatal(3));
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }

    @Test
    public void retryLimitedAuthFailures_failsFatal() throws Exception {
        subscribe(RxReprint.retryLimitedAuthFailures(2, 3));
        module.listener.onFailure(HARDWARE_UNAVAILABLE, true, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }

    @Test
    public void retryLimitedAuthFailures_passesUnlimitedNonFatal() throws Exception {
        subscribe(RxReprint.retryLimitedAuthFailures(2, 3));
        module.listener.onFailure(SENSOR_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(SENSOR_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(SENSOR_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(SENSOR_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(SENSOR_FAILED, false, "", module.TAG, 0);
        module.listener.onFailure(SENSOR_FAILED, false, "", module.TAG, 0);
        ts.assertNoValues();
        ts.assertNoTerminalEvent();
        module.listener.onSuccess(module.TAG);
        ts.assertValue(module.TAG);
        ts.assertCompleted();
    }

    @Test
    public void retryLimitedAuthFailures_passesLimitedTimeout() throws Exception {
        subscribe(RxReprint.retryLimitedAuthFailures(2, 3));
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }

    @Test
    public void retryLimitedAuthFailures_passesLimitedAuthFailure() throws Exception {
        subscribe(RxReprint.retryLimitedAuthFailures(2, 3));
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }

    @Test
    public void retryLimitedAuthFailures_passesMixedAuthFailureAndTimeout() throws Exception {
        subscribe(RxReprint.retryLimitedAuthFailures(2, 3));
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onSuccess(module.TAG);
        ts.assertValue(module.TAG);
        ts.assertCompleted();
    }

    @Test
    public void retryLimitedAuthFailures_failsMixedAuthFailureAndTimeoutOverLimit() throws Exception {
        subscribe(RxReprint.retryLimitedAuthFailures(2, 3));
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(TIMEOUT, true, "", module.TAG, 0);
        ts.assertNoTerminalEvent();
        module.listener.onFailure(AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        ts.assertError(AuthenticationFailure.class);
    }
}
