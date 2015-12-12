package com.github.ajalt.reprint.reactive;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

public class RxReprint {
    public static Observable<Void> authenticate() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Reprint.authenticateWithoutRestart(new AuthenticationListener() {
                    private boolean listening = true;

                    @Override
                    public void onSuccess() {
                        if (!listening) return;
                        listening = false;
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(null);
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull AuthenticationFailureReason failureReason, boolean fatal, @Nullable CharSequence errorMessage, int moduleTag, int errorCode) {
                        if (!listening) return;
                        final AuthenticationFailure result = new AuthenticationFailure(failureReason, fatal, errorMessage, moduleTag, errorCode);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(result);
                        }
                        if (fatal) {
                            listening = false;
                        }
                    }
                });
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Reprint.cancelAuthentication();
            }
        });
    }
}

