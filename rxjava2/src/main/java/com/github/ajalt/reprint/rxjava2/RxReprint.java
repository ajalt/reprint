package com.github.ajalt.reprint.rxjava2;


import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.AuthenticationResult;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.RestartPredicates;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Action;

import static com.github.ajalt.reprint.core.AuthenticationResult.Status.FATAL_FAILURE;
import static com.github.ajalt.reprint.core.AuthenticationResult.Status.NONFATAL_FAILURE;
import static com.github.ajalt.reprint.core.AuthenticationResult.Status.SUCCESS;

/**
 * RxJava 2 interface to Reprint authentication.
 */
public class RxReprint {
    /**
     * Return an {@link Flowable} that will continue to emit events as long as the fingerprint
     * sensor is active.
     *
     * @see Reprint#authenticate(AuthenticationListener)
     */
    public static Flowable<AuthenticationResult> authenticate() {
        return authenticate(RestartPredicates.defaultPredicate());
    }

    /**
     * Authenticate with a restart predicate.
     *
     * @param restartPredicate A predicate that controls the restart behavior.
     * @see #authenticate()
     * @see Reprint#authenticate(AuthenticationListener, Reprint.RestartPredicate)
     */
    public static Flowable<AuthenticationResult> authenticate(final Reprint.RestartPredicate restartPredicate) {
        return Flowable.create(new FlowableOnSubscribe<AuthenticationResult>() {
            @Override
            public void subscribe(final FlowableEmitter<AuthenticationResult> emitter) {
                Reprint.authenticate(new AuthenticationListener() {
                    private boolean listening = true;

                    @Override
                    public void onSuccess(int moduleTag) {
                        if (!listening) return;
                        listening = false;
                        emitter.onNext(new AuthenticationResult(SUCCESS, null, "", moduleTag, 0));
                        emitter.onComplete();
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
                            emitter.onComplete();
                        }
                    }
                }, restartPredicate);
            }
        }, BackpressureStrategy.LATEST).doOnCancel(new Action() {
            @Override
            public void run() throws Exception {
                Reprint.cancelAuthentication();
            }
        });
    }
}

