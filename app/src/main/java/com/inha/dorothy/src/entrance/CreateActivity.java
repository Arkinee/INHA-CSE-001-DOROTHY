package com.inha.dorothy.src.entrance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;

import java.util.ArrayList;

public class CreateActivity extends BaseActivity {

    private EditText mEdtTitle;
    private EditText mEdtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mEdtTitle = findViewById(R.id.edt_create_title);
        mEdtPassword = findViewById(R.id.edt_create_password);

    }

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.iv_create_back_arrow:
                finish();
                break;
            case R.id.btn_create:

                // 방 제목과 비밀번호 필수 입력
                if (mEdtTitle.getText().toString().equals("")) {
                    showCustomMessage(getString(R.string.create_room_title_input));
                    break;
                }

                if (mEdtPassword.getText().toString().equals("")) {
                    showCustomMessage(getString(R.string.create_room_password_input));
                    break;
                }

                Intent intent = new Intent();
                intent.putExtra("title", mEdtTitle.getText().toString());
                intent.putExtra("password", mEdtPassword.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
