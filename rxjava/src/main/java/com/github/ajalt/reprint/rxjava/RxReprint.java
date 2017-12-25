package com.github.ajalt.reprint.rxjava;


import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.AuthenticationResult;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.RestartPredicates;

import rx.Emitter;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

import static com.github.ajalt.reprint.core.AuthenticationResult.Status.FATAL_FAILURE;
import static com.github.ajalt.reprint.core.AuthenticationResult.Status.NONFATAL_FAILURE;
import static com.github.ajalt.reprint.core.AuthenticationResult.Status.SUCCESS;

/**
 * RxJava 1 interface to Reprint authentication.
 */
public class RxReprint {
    /**
     * Return an {@link Observable} that will continue to emit events as long as the fingerprint
     * sensor is active.
     *
     * @see Reprint#authenticate(AuthenticationListener)
     */
    public static Observable<AuthenticationResult> authenticate() {
        return authenticate(RestartPredicates.defaultPredicate());
    }

    /**
     * Authenticate with a restart predicate.
     *
     * @param restartPredicate A predicate that controls the restart behavior.
     * @see #authenticate()
     * @see Reprint#authenticate(AuthenticationListener, Reprint.RestartPredicate)
     */
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
                    public void onFailure(AuthenticationFailureReason failureReason,
                                          boolean fatal, CharSequence errorMessage,
                                          int moduleTag, int errorCode) {
                        if (!listening) return;

                        emitter.onNext(new AuthenticationResult(
                                fatal ? FATAL_FAILURE : NONFATAL_FAILURE,
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

