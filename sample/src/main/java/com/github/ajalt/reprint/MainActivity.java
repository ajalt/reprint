package com.github.ajalt.reprint;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.reactive.AuthenticationResult;
import com.github.ajalt.reprint.reactive.RxReprint;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.result)
    TextView result;

    @Bind(R.id.hardware_present)
    TextView hardwarePresent;

    @Bind(R.id.fingerprints_registered)
    TextView fingerprintsRegistered;

    @Bind(R.id.rx_switch)
    CompoundButton rxSwitch;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        hardwarePresent.setText(String.valueOf(Reprint.isHardwarePresent()));
        fingerprintsRegistered.setText(String.valueOf(Reprint.hasFingerprintRegistered()));

        running = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancel();
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        if (running) {
            cancel();
        } else {
            start();
        }
    }

    private void start() {
        running = true;
        result.setText("Listening");
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white_24dp));

        if (rxSwitch.isChecked()) {
            startReactive();
        } else {
            startTraditional();
        }
    }

    private void startTraditional() {
        Reprint.authenticate(new AuthenticationListener() {
            @Override
            public void onSuccess() {
                showSuccess();
            }

            @Override
            public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                  @Nullable CharSequence errorMessage, int fromModule, int errorCode) {
                showError(failureReason, fatal, errorMessage, errorCode);
            }
        });
    }

    private void startReactive() {
        RxReprint.authenticate()
                .subscribe(new Action1<AuthenticationResult>() {
                    @Override
                    public void call(AuthenticationResult r) {
                        if (r.failureReason == null) {
                            showSuccess();
                        } else {
                            showError(r.failureReason, r.fatal, r.errorMessage, r.errorCode);
                        }
                    }
                });
    }

    private void cancel() {
        result.setText("Cancelled");
        running = false;
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fingerprint_white_24dp));
        Reprint.cancelAuthentication();
    }

    private void showSuccess() {
        result.setText("Success");
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fingerprint_white_24dp));
        running = false;
    }

    private void showError(AuthenticationFailureReason failureReason, boolean fatal, @Nullable CharSequence errorMessage, int errorCode) {
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

        if (fatal) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_fingerprint_white_24dp));
            running = false;
        }
    }
}
