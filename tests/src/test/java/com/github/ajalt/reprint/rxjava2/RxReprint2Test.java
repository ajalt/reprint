package com.github.ajalt.reprint.rxjava2;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationResult;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.testing.TestReprintModule;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.reactivex.subscribers.TestSubscriber;

import static com.github.ajalt.reprint.core.AuthenticationResult.Status.FATAL_FAILURE;
import static com.github.ajalt.reprint.core.AuthenticationResult.Status.NONFATAL_FAILURE;
import static com.github.ajalt.reprint.core.AuthenticationResult.Status.SUCCESS;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


public class RxReprint2Test {
    public TestReprintModule module;
    public TestSubscriber<AuthenticationResult> ts;

    @Before
    public void setup() {
        module = new TestReprintModule();
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
        List<AuthenticationResult> events = ts.values();
        assertEquals(events.size(), 1);
        assertEquals(events.get(0).status, SUCCESS);
        ts.assertComplete();
    }

    @Test
    public void successfulRequestBackpressure() throws Exception {
        module.listener.onSuccess(module.TAG);
        ts.assertNoValues();
        ts.requestMore(1);
        ts.assertValueCount(1);
        ts.assertComplete();
    }

    @Test
    public void nonFatalFailure() throws Exception {
        ts.requestMore(1);
        module.listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        List<AuthenticationResult> events = ts.values();
        assertEquals(events.size(), 1);
        assertEquals(events.get(0).status, NONFATAL_FAILURE);
        ts.assertNotTerminated();
    }

    @Test
    public void fatalFailure() throws Exception {
        ts.requestMore(1);
        module.listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, true, "", module.TAG, 0);
        List<AuthenticationResult> events = ts.values();
        assertEquals(events.size(), 1);
        assertEquals(events.get(0).status, FATAL_FAILURE);
        ts.assertComplete();
    }

    @Test
    public void unsubscribe_cancels() throws Exception {
        assertFalse(module.cancellationSignal.isCanceled());
        ts.dispose();
        assertTrue(module.cancellationSignal.isCanceled());
    }
}
