package com.github.ajalt.reprint.core;

import org.junit.Test;

import static com.github.ajalt.reprint.core.AuthenticationFailureReason.AUTHENTICATION_FAILED;
import static com.github.ajalt.reprint.core.AuthenticationFailureReason.HARDWARE_UNAVAILABLE;
import static com.github.ajalt.reprint.core.AuthenticationFailureReason.TIMEOUT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestartPredicateTest {

    @Test
    public void defaultPredicate_passesUnlimitedNonFatal() throws Exception {
        Reprint.RestartPredicate predicate = RestartPredicates.defaultPredicate();
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 0));
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 1));
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 2));
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 3));
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 4));
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 5));
    }

    @Test
    public void defaultPredicate_passesLimitedTimeout() throws Exception {
        Reprint.RestartPredicate predicate = RestartPredicates.defaultPredicate();
        assertTrue(predicate.invoke(TIMEOUT, 0));
        assertTrue(predicate.invoke(TIMEOUT, 1));
        assertTrue(predicate.invoke(TIMEOUT, 2));
        assertTrue(predicate.invoke(TIMEOUT, 3));
        assertTrue(predicate.invoke(TIMEOUT, 4));

        assertFalse(predicate.invoke(TIMEOUT, 5));
    }

    @Test
    public void restartTimeoutsPredicate_passesConfigurableTimeout() throws Exception {
        Reprint.RestartPredicate predicate = RestartPredicates.restartTimeouts(2);
        assertTrue(predicate.invoke(TIMEOUT, 0));
        assertTrue(predicate.invoke(TIMEOUT, 1));

        assertFalse(predicate.invoke(TIMEOUT, 2));
    }

    @Test
    public void defaultPredicate_passesMixedAuthFailureAndTimeout() throws Exception {
        Reprint.RestartPredicate predicate = RestartPredicates.defaultPredicate();
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 0));
        assertTrue(predicate.invoke(TIMEOUT, 1));
        assertTrue(predicate.invoke(AUTHENTICATION_FAILED, 2));
        assertTrue(predicate.invoke(TIMEOUT, 3));
        assertTrue(predicate.invoke(TIMEOUT, 4));
        assertTrue(predicate.invoke(TIMEOUT, 5));
        assertTrue(predicate.invoke(TIMEOUT, 6));

        assertFalse(predicate.invoke(TIMEOUT, 7));
    }

    @Test
    public void neverRestartPredicate_neverRestarts() throws Exception {
        Reprint.RestartPredicate predicate = RestartPredicates.neverRestart();
        assertFalse(predicate.invoke(TIMEOUT, 0));
        assertFalse(predicate.invoke(AUTHENTICATION_FAILED, 1));
        assertFalse(predicate.invoke(HARDWARE_UNAVAILABLE, 2));
    }
}
