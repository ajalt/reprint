package com.github.ajalt.reprint.reactive;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.RestartPredicates;

import rx.Emitter;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

import static com.github.ajalt.reprint.reactive.AuthenticationResult.Status.RECOVERABLE_FAILURE;
import static com.github.ajalt.reprint.reactive.AuthenticationResult.Status.SUCCESS;
import static com.github.ajalt.reprint.reactive.AuthenticationResult.Status.UNRECOVERABLE_FAILURE;

/** RxJava interface to Reprint authentication. */
public class RxReprint {
    /**
     * Return an {@link Observable} that will continue to emit events as long as the fingerprint
     * sensor is active.
     */
    public static Observable<AuthenticationResult> authenticate() {
        return authenticate(RestartPredicates.defaultPredicate());
    }

    /** @see #authenticate() */
    public static Observable<AuthenticationResult> authenticate(final Reprint.RestartPredicate restartPredicate) {
        return Observable.create(new Action1<Emitter<AuthenticationResult>>() {
            @Override
            public void call(final Emitter<AuthenticationResult> emitter) {
                Reprint.authenticate(new AuthenticationListener() {
                    private boolean listening = true;

                    @Override
                    public void onSuccess(int moduleTag) {
                        if (!listening) return;
                        listening = false;
                        emitter.onNext(new AuthenticationResult(SUCCESS, null, "", moduleTag, 0));
                        emitter.onCompleted();
                    }

                    @Override
                    public void onFailure(@NonNull AuthenticationFailureReason failureReason,
                                          boolean fatal, @Nullable CharSequence errorMessage,
                                          int moduleTag, int errorCode) {
                        if (!listening) return;

                        emitter.onNext(new AuthenticationResult(
                                fatal ? UNRECOVERABLE_FAILURE : RECOVERABLE_FAILURE,
                                failureReason, errorMessage, moduleTag, errorCode));
                        if (fatal) {
                            emitter.onCompleted();
                        }
                    }
                }, restartPredicate);
            }
        }, Emitter.BackpressureMode.LATEST).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Reprint.cancelAuthentication();
            }
        });
    }
}

