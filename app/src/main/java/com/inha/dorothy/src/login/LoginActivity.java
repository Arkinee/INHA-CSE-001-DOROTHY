package com.inha.dorothy.src.login;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static Boolean mStayLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
