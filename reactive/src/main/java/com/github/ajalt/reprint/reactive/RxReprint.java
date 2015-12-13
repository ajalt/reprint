package com.github.ajalt.reprint.reactive;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.ReprintModule;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * ReactiveX interface to reprint authentication.
 */
public class RxReprint {
    /**
     * Return an {@link Observable} whose {@link Subscriber#onNext(Object)} will be called at most
     * once.
     * <p/>
     * The argument is the {@link ReprintModule#tag()} of the module that was used for
     * authentication. Any failures will cause {@link Subscriber#onNext(Object)} to be called with
     * an {@link AuthenticationFailure} containing the reason for the failure.
     * <p/>
     * When either onNext or onFailure is called, the sensor will be off, so you will usually want
     * to resubscribe if the failure is non-fatal.
     */
    public static Observable<Integer> authenticate() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(final Subscriber<? super Integer> subscriber) {
                Reprint.authenticateWithoutRestart(new AuthenticationListener() {
                    private boolean listening = true;

                    @Override
                    public void onSuccess(int moduleTag) {
                        if (!listening) return;
                        listening = false;
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(moduleTag);
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

