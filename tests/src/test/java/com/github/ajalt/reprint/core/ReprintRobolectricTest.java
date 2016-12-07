package com.github.ajalt.reprint.core;

import com.github.ajalt.library.BuildConfig;
import com.github.ajalt.reprint.testing.TestApp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23, application = TestApp.class)
public class ReprintRobolectricTest {
    @Test
    public void reprint_initialize() throws Exception {
        Reprint.initialize(RuntimeEnvironment.application);
    }
}
