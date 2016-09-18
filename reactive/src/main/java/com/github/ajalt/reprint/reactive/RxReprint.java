package com.github.ajalt.reprint.reactive;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.core.ReprintModule;

import rx.AsyncEmitter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.github.ajalt.reprint.core.AuthenticationFailureReason.AUTHENTICATION_FAILED;
import static com.github.ajalt.reprint.core.AuthenticationFailureReason.TIMEOUT;

/**
 * ReactiveX interface to reprint authentication.
 */
public class RxReprint {
    /**
     * Return an {@link Observable} whose {@link Subscriber#onNext(Object)} will be called at most
     * once.
     * <p>
     * The argument is the {@link ReprintModule#tag()} of the module that was used for
     * authentication. Any failures will cause {@link Subscriber#onNext(Object)} to be called with
     * an {@link AuthenticationFailure} containing the reason for the failure.
     * <p>
     * When either onNext or onFailure is called, the sensor will be off, so you will usually want
     * to resubscribe if the failure is non-fatal.
     */
    public static Observable<Integer> authenticate() {
        return Observable.fromEmitter(new Action1<AsyncEmitter<Integer>>() {
            @Override
            public void call(final AsyncEmitter<Integer> emitter) {
                Reprint.authenticateWithoutRestart(new AuthenticationListener() {
                    private boolean listening = true;

                    @Override
                    public void onSuccess(int moduleTag) {
                        if (!listening) return;
                        listening = false;
                        emitter.onNext(moduleTag);
                        emitter.onCompleted();
                    }

                    @Override
                    public void onFailure(@NonNull AuthenticationFailureReason failureReason,
                                          boolean fatal, @Nullable CharSequence errorMessage,
                                          int moduleTag, int errorCode) {
                        if (!listening) return;
                        emitter.onError(new AuthenticationFailure(
                                failureReason, fatal, errorMessage, moduleTag, errorCode));
                        if (fatal) {
                            listening = false;
                        }
                    }
                });
            }
        }, AsyncEmitter.BackpressureMode.LATEST).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                Reprint.cancelAuthentication();
            }
        });
    }

    /**
     * Returns a predicate suitable for passing to {@link Observable#retry()} that will retry on a
     * non-fatal authentication error.
     * <p>
     * Since the observable returned by {@link #authenticate()} will terminate when a recoverable
     * error happens, it's a common pattern to report help text in {@link
     * Observable#doOnError(Action1)} and call {@link Observable#retry()} to restart the
     * subscription when the error is non-fatal.
     * <p>
     * This predicate will restart on timeout up to `maxTimeout` times, or indefinitely for
     * non-fatal errors.
     * <p>
     * You shouldn't restart indefinitely on timeout, since an active sensor consumes a lot of
     * battery.
     *
     * @param maxTimeoutCount The maximum number of times to retry on timeout. All other non-fatal
     *                        errors will be restarted indefinitely.
     */
    public static Func2<Integer, Throwable, Boolean> retryNonFatal(final int maxTimeoutCount) {
        return new Func2<Integer, Throwable, Boolean>() {
            @Override
            public Boolean call(Integer count, Throwable throwable) {
                if (!(throwable instanceof AuthenticationFailure)) return false;
                AuthenticationFailure e = (AuthenticationFailure) throwable;
                return !e.fatal || e.failureReason == TIMEOUT && count <= maxTimeoutCount;
            }
        };
    }

    /**
     * Returns a predicate suitable for passing to {@link Observable#retry()} that behaves like
     * {@link #retryNonFatal(int)}, but additionally limits the number of authentication failures.
     * <p>
     * Note that the system may lock the sensor after a smaller number of authentication failures
     * than you ask for. In that case, there's no way to restart the authentication until the sensor
     * becomes unlocked.
     */
    public static Func2<Integer, Throwable, Boolean> retryLimitedAuthFailures(
            final int maxAuthFailures, final int maxTimeoutCount) {
        return new Func2<Integer, Throwable, Boolean>() {
            private int authFailureCount = 0;
            private int timeoutFailureCount = 0;

            @Override
            public Boolean call(Integer count, Throwable throwable) {
                if (!(throwable instanceof AuthenticationFailure)) return false;
                AuthenticationFailure e = (AuthenticationFailure) throwable;
                if (e.failureReason == TIMEOUT) return timeoutFailureCount++ < maxTimeoutCount;
                if (e.fatal) return false;
                if (e.failureReason == AUTHENTICATION_FAILED)
                    return authFailureCount++ < maxAuthFailures;
                return true;
            }
        };
    }
}

