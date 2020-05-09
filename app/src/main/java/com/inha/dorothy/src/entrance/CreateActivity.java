package com.inha.dorothy.src.entrance;

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

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRoomReference = mFirebaseDatabase.getReference();

    private ValueEventListener roomListener;
    ArrayList<Room> roomList;
    int mRoomIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        mEdtTitle = findViewById(R.id.edt_create_title);
        mEdtPassword = findViewById(R.id.edt_create_password);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mRoomIndex = 0;
        roomList = new ArrayList<>();

        roomListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Room room = ds.getValue(Room.class);
                    roomList.add(room);
                }

                mRoomIndex = roomList.size();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRoomReference.addListenerForSingleValueEvent(roomListener);

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.iv_create_back_arrow:
                finish();
                break;
            case R.id.btn_create:

                // 방 제목과 비밀번호 필수 입력
                if(mEdtTitle.getText().toString().equals("")) break;
                if(mEdtPassword.getText().toString().equals("")) break;

//                mRoomReference.

                break;
        }
    }
}
