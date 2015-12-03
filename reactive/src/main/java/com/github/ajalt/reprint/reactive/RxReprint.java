package com.github.ajalt.reprint.reactive;


import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

public class RxReprint {
    public static Observable<AuthenticationResult> authenticate() {
        return authenticate(Reprint.DEFAULT_RESTART_COUNT);
    }

    public static Observable<AuthenticationResult> authenticate(final int restartCount) {
        return Observable.create(new Observable.OnSubscribe<AuthenticationResult>() {
            @Override
            public void call(final Subscriber<? super AuthenticationResult> subscriber) {
                Reprint.authenticate(new AuthenticationListener() {
                    private boolean listening = true;

                    @Override
                    public void onSuccess() {
                        if (!listening) return;
                        listening = false;
                        subscriber.onNext(new AuthenticationResult(null, true, null, 0, 0));
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, @Nullable CharSequence errorMessage, int fromModule, int errorCode) {
                        if (!listening) return;
                        final AuthenticationResult result = new AuthenticationResult(failureReason, fatal, errorMessage, fromModule, errorCode);
                        subscriber.onNext(result);
                        if (fatal) {
                            listening = false;
                            subscriber.onCompleted();
                        }
                    }
                }, restartCount);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Reprint.cancelAuthentication();
            }
        });
    }
}
