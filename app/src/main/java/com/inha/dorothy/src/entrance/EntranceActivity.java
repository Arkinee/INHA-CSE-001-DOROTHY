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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Map;

public class EntranceActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRoomReference = mFirebaseDatabase.getReference().child("room").child("room_id");

    HashMap<String, Object> childUpdates = null;
    Map<String, Object> roomValue = null;

    static int CREATE_ID = 1000;
    private RecyclerView mRvRoom;
    private RoomAdapter mAdapter;
    private ArrayList<Room> mRoomArrayList;
    private EditText mEdtEntrance;
    private String mRoomUniqueKey;

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

                // DB에 변화가 있을시 mRoomArrayList에 key값과 변수들을 불러온다.
                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()) {
                        DataSnapshot snap = iter.next();
                        RoomInfo info = snap.child("RoomInfo").getValue(RoomInfo.class);//room_id의 child Roominfo의 값들을 가져온다.
                        mRoomArrayList.add(new Room(String.valueOf(snap.getKey()), info));
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //방클릭 이벤트
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
                startActivityForResult(intent, CREATE_ID);
                return true;
            case R.id.remove:
                showCustomMessage("방 삭제");
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CREATE_ID){
            assert data != null;
            boolean flag = true;
            String title = data.getStringExtra("title");
            String password = data.getStringExtra("password");

            for(Room room : mRoomArrayList){
                if(room.info.title.equals(title)){
                    flag = false;
                    showCustomMessage(getString(R.string.create_room_already_same_title));
                    break;
                }
            }

            Long person = 0L;
            Long doodle = 0L;

            RoomInfo room = new RoomInfo(title, password, person, doodle);
            postFirebaseDatabase(room, flag);

        }
    }

    public void postFirebaseDatabase(RoomInfo room, boolean flag){
        DatabaseReference mCreateReference = mFirebaseDatabase.getReference();
        HashMap<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;
        if(flag) {
            postValues = room.toMap();

            /**NOTICE 삭제 수정이 반복되면 mRoomArrayList.size가 중복되어 생성될 가능성이 있음
            따라서 mRoomReference.push().getKey() 를 통해 room_id 하위 child 값으로 들어가는 고유 키값을 생성**/
//            childUpdates.put("/room/room_id/" + mRoomArrayList.size() + "/RoomInfo", postValues);

            mRoomUniqueKey= mRoomReference.child("room_id").push().getKey();
            childUpdates.put("/room/room_id/" + mRoomUniqueKey + "/RoomInfo", postValues);
            mCreateReference.updateChildren(childUpdates);
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
