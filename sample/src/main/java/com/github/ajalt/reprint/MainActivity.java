package com.github.ajalt.reprint;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final TextView hardwarePresent = (TextView) findViewById(R.id.hardware_present);
        final TextView fingerprintsRegistered = (TextView) findViewById(R.id.fingerprints_registered);
        result = (TextView) findViewById(R.id.result);

        hardwarePresent.setText(String.valueOf(Reprint.isHardwarePresent()));
        fingerprintsRegistered.setText(String.valueOf(Reprint.hasFingerprintRegistered()));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText("listening");
                Reprint.authenticate(listener);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Reprint.cancelAuthentication();
        result.setText("cancelled");
    }

    private AuthenticationListener listener = new AuthenticationListener() {
        @Override
        public void onSuccess() {
            result.setText("success");
        }

        @Override
        public void onFailure(int fromModule, AuthenticationFailureReason failureReason, boolean fatal, int errorCode, @Nullable CharSequence errorMessage) {
            CharSequence message = "";
            if (errorMessage != null) {
                message = errorMessage;
            } else {
                switch (failureReason) {
                    case NO_HARDWARE:
                        message = "Device does not have a sensor or does not have registered fingerprints";
                        break;
                    case HARDWARE_UNAVAILABLE:
                        message = "Fingerprint reader temporarily unavailable";
                        break;
                    case NO_FINGERPRINTS_REGISTERED:
                        message = "No registered fingerprints.";
                        break;
                    case SENSOR_FAILED:
                        message = "Could not read fingerprint";
                        break;
                    case LOCKED_OUT:
                        message = "Too many incorrect attempts";
                        break;
                    case TIMEOUT:
                        message = "Cancelled due to inactivity";
                        break;
                    case AUTHENTICATION_FAILED:
                        message = "Fingerprint not recognized";
                        break;
                    case UNKNOWN:
                        message = "Could not read fingerprint";
                        break;
                }
            }
            result.setText(message + (fatal ? "." : ". Try again.") + " (error code: " + errorCode + ')');
        }
    };
}
