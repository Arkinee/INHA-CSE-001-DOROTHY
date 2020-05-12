package com.inha.dorothy.src.splash;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;
import com.inha.dorothy.src.login.LoginActivity;

import static com.inha.dorothy.src.splash.Network.isMOBILEConnected;
import static com.inha.dorothy.src.splash.Network.isWIFIConnected;

public class SplashActivity extends BaseActivity {

    private Handler mHandler; // 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //네트워크 상태가 online일때 로그인 화면으로 이동
        //offline일 경우 다이얼로그를 통해 다시 네트워크 상태 체크
        if (isNetworkOnline()) {
            mHandler = new Handler();
            mHandler.postDelayed(mrun, 1500);   // 1.5초 딜레이
        } else {
            final AlertDialog.Builder onlineDialog = new AlertDialog.Builder(this);
            onlineDialog.setTitle("Online Check");
            onlineDialog.setMessage("Network connection fail!");
            onlineDialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            onlineDialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 네트워크 연결 체크 재시도
                    if (isNetworkOnline()) {
                        startLoginActivity();
                    } else {
                        onlineDialog.show();
                    }
                }
            });
            onlineDialog.show();
        }

    }


    //뒤로가기 누를시 removeCallbacks을 통해 일정 시간 뒤 실행될 runnable 취소
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isNetworkOnline()) finish();
        else mHandler.removeCallbacks(mrun);
    }

    /**
     * 네트워크 상태 체크
     * Network Class에서 static 함수인 isWIFIConnected와 isMOBILEConnected 사용했다.
     * 위 두함수는 각각 WIFI와 모바일인터넷(LTE) 가 연결되었을 시 TRUE를 반환한다.
     * isNetworkOnline 함수의 return 값은 비행기모드,네트워크 연결이 되지 않았을 경우 false 그 외 네트워크 연결이 원활할 경우 true를 반환한다.
     **/
    public boolean isNetworkOnline() {
        return isWIFIConnected(this) || isMOBILEConnected(this);
    }

    //로그인 화면으로 이동
    public void startLoginActivity() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        LoginActivity.mStayLogin = true;
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    //Runnable 함수
    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            startLoginActivity();
        }
    };

}
