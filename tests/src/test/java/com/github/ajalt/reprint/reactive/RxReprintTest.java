package com.github.ajalt.reprint.reactive;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.testing.TestReprintModule;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

import static com.github.ajalt.reprint.reactive.AuthenticationResult.Status.RECOVERABLE_FAILURE;
import static com.github.ajalt.reprint.reactive.AuthenticationResult.Status.SUCCESS;
import static com.github.ajalt.reprint.reactive.AuthenticationResult.Status.UNRECOVERABLE_FAILURE;
import static org.junit.Assert.assertEquals;


public class RxReprintTest {
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
        final List<AuthenticationResult> events = ts.getOnNextEvents();
        assertEquals(events.size(), 1);
        assertEquals(events.get(0).status, SUCCESS);
        ts.assertCompleted();
    }

    @Test
    public void successfulRequestBackpressure() throws Exception {
        module.listener.onSuccess(module.TAG);
        ts.assertNoValues();
        ts.requestMore(1);
        ts.assertValueCount(1);
        ts.assertCompleted();
    }

    @Test
    public void nonFatalFailure() throws Exception {
        ts.requestMore(1);
        module.listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, "", module.TAG, 0);
        final List<AuthenticationResult> events = ts.getOnNextEvents();
        assertEquals(events.size(), 1);
        assertEquals(events.get(0).status, RECOVERABLE_FAILURE);
        ts.assertNoTerminalEvent();
    }

    @Test
    public void fatalFailure() throws Exception {
        ts.requestMore(1);
        module.listener.onFailure(AuthenticationFailureReason.AUTHENTICATION_FAILED, true, "", module.TAG, 0);
        final List<AuthenticationResult> events = ts.getOnNextEvents();
        assertEquals(events.size(), 1);
        assertEquals(events.get(0).status, UNRECOVERABLE_FAILURE);
        ts.assertCompleted();
    }
}
