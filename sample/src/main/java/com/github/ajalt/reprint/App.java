package com.github.ajalt.reprint;

import android.app.Application;

import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.module.marshmallow.MarshmallowReprintModule;
import com.github.ajalt.reprint.module.spass.SpassReprintModule;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Reprint.instance()
                .registerModule(new MarshmallowReprintModule(this))
                .registerModule(new SpassReprintModule(this));
    }
}
