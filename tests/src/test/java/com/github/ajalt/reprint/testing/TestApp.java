package com.github.ajalt.reprint.testing;

import android.app.Application;

import com.github.ajalt.reprint.core.Reprint;

public class TestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Reprint.initialize(this);
    }
}
