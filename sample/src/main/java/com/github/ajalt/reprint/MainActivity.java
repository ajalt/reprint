package com.github.ajalt.reprint;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.github.ajalt.reprint.reactive.AuthenticationFailure;
import com.github.ajalt.reprint.reactive.RxReprint;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp));

        if (rxSwitch.isChecked()) {
            startReactive();
        } else {
            startTraditional();
        }
    }

    private void startTraditional() {
        Reprint.authenticate(new AuthenticationListener() {
            @Override
            public void onSuccess(int moduleTag) {
                showSuccess();
            }

            @Override
            public void onFailure(@NonNull AuthenticationFailureReason failureReason, boolean fatal,
                                  @Nullable CharSequence errorMessage, int moduleTag, int errorCode) {
                showError(failureReason, fatal, errorMessage, errorCode);
            }
        });
    }

    private void startReactive() {
        RxReprint.authenticate()
                .doOnError(throwable -> {
                    AuthenticationFailure e = (AuthenticationFailure) throwable;
                    showError(e.failureReason, e.fatal, e.errorMessage, e.errorCode);
                }).retry(RxReprint.retryNonFatal(5))
                .subscribe(tag -> showSuccess(), e -> {});
    }

    private void cancel() {
        result.setText("Cancelled");
        running = false;
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fingerprint_white_24dp));
        Reprint.cancelAuthentication();
    }

    private void showSuccess() {
        result.setText("Success");
        fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fingerprint_white_24dp));
        running = false;
    }

    private void showError(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int errorCode) {
        result.setText(errorMessage);

        if (fatal) {
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fingerprint_white_24dp));
            running = false;
        }
    }
}
