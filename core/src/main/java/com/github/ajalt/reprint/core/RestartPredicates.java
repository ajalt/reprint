package com.github.ajalt.reprint.core;

import static com.github.ajalt.reprint.core.AuthenticationFailureReason.TIMEOUT;

public class RestartPredicates {
    /**
     * A predicate that will retry all non-fatal failures indefinitely, and timeouts a given number
     * of times.
     */
    public static Reprint.RestartPredicate restartTimeouts(final int timeoutRestartCount) {
        return new Reprint.RestartPredicate() {
            @Override
            public boolean invoke(AuthenticationFailureReason reason, int restartCount) {
                return reason != TIMEOUT || restartCount <= timeoutRestartCount;
            }
        };
    }

    /** A predicate that will retry all non-fatal failures indefinitely, and timeouts 5 times. */
    public static Reprint.RestartPredicate defaultPredicate() {
        return restartTimeouts(5);
    }

    /** A predicate that will never restart after any failure. */
    public static Reprint.RestartPredicate neverRestart() {
        return new Reprint.RestartPredicate() {
            @Override
            public boolean invoke(AuthenticationFailureReason reason, int restartCount) {
                return false;
            }
        };
    }
}
