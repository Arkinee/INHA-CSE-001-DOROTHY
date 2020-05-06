package com.inha.dorothy.src.entrance;

import android.os.Bundle;
import android.view.View;

import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;

public class EntranceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_entrance_back_arrow:
                finish();
                break;
            case R.id.iv_entrance_search:

                break;
            case R.id.iv_entrance_menu:
                break;
        }
    }
}
