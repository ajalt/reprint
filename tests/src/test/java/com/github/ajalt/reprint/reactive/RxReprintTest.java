package com.github.ajalt.reprint.reactive;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.testing.TestReprintModule;

import org.junit.Before;
import org.junit.Test;

import rx.observers.TestSubscriber;

public class RxReprintTest {
    public TestReprintModule module;
    public TestSubscriber<Integer> ts;

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
}
