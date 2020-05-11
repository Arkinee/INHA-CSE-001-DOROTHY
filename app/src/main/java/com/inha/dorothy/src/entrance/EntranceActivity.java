package com.inha.dorothy.src.entrance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;
import com.inha.dorothy.src.draw.DrawingActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EntranceActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRoomReference = mFirebaseDatabase.getReference().child("room").child("room_id");

    private RecyclerView mRvRoom;
    private RoomAdapter mAdapter;

    private ArrayList<Room> mRoomArrayList;

    private EditText mEdtEntrance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);

        mRoomArrayList = new ArrayList<>();
        mEdtEntrance = findViewById(R.id.edt_entrance_search);
        mEdtEntrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtEntrance.setInputType(1);
            }
        });

        mRvRoom = findViewById(R.id.rv_entrance);
        mRvRoom.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RoomAdapter(this, mRoomArrayList);
        mRvRoom.setAdapter(mAdapter);

        ImageView ivMenu = findViewById(R.id.iv_entrance_menu);
        registerForContextMenu(ivMenu);

        mRoomReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRoomArrayList.clear();

                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()) {
                        DataSnapshot snap = iter.next();
                        Log.d("로그", "key: " + snap.getKey());
                        RoomInfo info = snap.child("RoomInfo").getValue(RoomInfo.class);
                        Log.d("로그", "info: " + info.title);
                        mRoomArrayList.add(new Room(Integer.valueOf(snap.getKey()), info));
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAdapter.setOnItemClickListener(new RoomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Intent intent = new Intent(getApplicationContext(), DrawingActivity.class);
                intent.putExtra("room_id", mRoomArrayList.get(pos).id);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onMenuItemClick(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create:
                Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivity(intent);
                return true;
            case R.id.remove:
                showCustomMessage("방 삭제");
                return true;
            default:
                return false;
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_entrance_back_arrow:
                finish();
                break;
            case R.id.iv_entrance_search:

                break;
            case R.id.iv_entrance_menu:
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.setOnMenuItemClickListener(this);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.entrance_menu, popupMenu.getMenu());
                popupMenu.show();
                break;
        }
    }
}
