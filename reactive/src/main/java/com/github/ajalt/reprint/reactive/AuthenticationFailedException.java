package com.github.ajalt.reprint.reactive;

public class AuthenticationFailedException extends Exception {
    public final AuthenticationResult result;

    public AuthenticationFailedException(AuthenticationResult result) {this.result = result;}
}
