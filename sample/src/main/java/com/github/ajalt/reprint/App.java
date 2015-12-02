package com.github.ajalt.reprint;

import android.app.Application;

import com.github.ajalt.reprint.core.Reprint;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Reprint.initialize(this);
    }
}
