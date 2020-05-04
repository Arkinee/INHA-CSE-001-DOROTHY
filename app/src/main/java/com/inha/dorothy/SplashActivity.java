package com.inha.dorothy;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends BaseActivity {

    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (!isOnline()) {
            final AlertDialog.Builder onlineDialog = new AlertDialog.Builder(this);
            onlineDialog.setTitle("Online Check");
            onlineDialog.setMessage("You are Offline T.T");
            onlineDialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });

            onlineDialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (isOnline()) {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        LoginActivity.stayLogin = true;
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        onlineDialog.show();
                    }
                }
            });
            onlineDialog.show();
        } else {
            mHandler = new Handler();
            mHandler.postDelayed(mrun, 3000);
        }
    }

    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            LoginActivity.stayLogin = true;
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isOnline()) finish();
        else mHandler.removeCallbacks(mrun);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
