package com.inha.dorothy.src.entrance;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;

public class CreateActivity extends BaseActivity {

    private EditText mEdtTitle;
    private EditText mEdtPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mEdtTitle = findViewById(R.id.edt_create_title);
        mEdtPassword = findViewById(R.id.edt_create_password);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_create_back_arrow:
                finish();
                break;
            case R.id.btn_create:
                mDatabaseReference.child("room_index").
                break;
        }
    }
}
