package com.inha.dorothy.src.mydraw;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.ItemDecoration;
import com.inha.dorothy.R;
import com.inha.dorothy.src.entrance.Room;
import com.inha.dorothy.src.entrance.RoomInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyDrawActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener{

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseDatabase mFirebase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = mFirebase.getReference();
    private DatabaseReference mRoomTitle = mFirebase.getReference("room").child("room_id");
    private DatabaseReference mDoodleRef = mRef.child("user").child(mUser.getUid());
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private ArrayList<MyDraw> mDrawArrayList;
    private ArrayList<DrawPerRoom> mRoomList;
    private ArrayList<String> mDoodleList;
    private DrawAdapter mAdapter;

    private TextView mTvNumOfDraw;
    private TextView mTvCheckRemove;
    private String mTitle;

    private boolean mRemove;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_draw);

        //사용할 컴포넌트들 초기화
        RecyclerView rvMyDraw = findViewById(R.id.rv_my_draw);
        rvMyDraw.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        rvMyDraw.addItemDecoration(new ItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing),
                getResources().getInteger(R.integer.draw_list_columns)));

        mTvNumOfDraw = findViewById(R.id.tv_my_draw_number);
        mTvCheckRemove = findViewById(R.id.tv_my_draw_remove);

        mDrawArrayList = new ArrayList<>();
        mRoomList = new ArrayList<>();
        mDoodleList = new ArrayList<>();
        mRemove = false;
        mContext = this;

        mAdapter = new DrawAdapter(this, mDrawArrayList);
        rvMyDraw.setAdapter(mAdapter);

        //데이터베이스에 접근해 각 방과 로그인된 유저의 정보 가져오기
        accessDatabase();

        //그림 클릭 이벤트
        mAdapter.setOnItemClickListener(new DrawAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                if(!mRemove){   // '삭제하기' 중이 아닌 경우
                    // 선택한 그림이 포함된 방의 위치를 다이얼로그 형태로 보여줌
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getString(R.string.my_draw_dialog_title)).setMessage(mDrawArrayList.get(pos).room);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else{
                    // '삭제하기' 중인 경우 선택된 상태에 따라 선택, 선택안됨으로 update시켜줌
                    if(!mDrawArrayList.get(pos).isCheck) {
                        mDrawArrayList.get(pos).isCheck = true;
                        mAdapter.notifyDataSetChanged();
                    }else{
                        mDrawArrayList.get(pos).isCheck = false;
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        // 사용자가 그림을 지우면 사용자가 그린 그림의 개수 동기화
        mDoodleRef.child("downloadURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDoodleList.clear();

                if(dataSnapshot.hasChildren()){
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()){
                        DataSnapshot snap = iter.next();
                        String doodle = snap.getKey();
                        mDoodleList.add(doodle);
                    }
                }
                mDoodleRef.child("doodles").setValue(mDoodleList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    void accessDatabase() {
        // 방마다 가지고 있는 그림의 정보를 매칭해서 ArrayList에 저장
        mRoomTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRoomList.clear();

                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()) {
                        DataSnapshot snap = iter.next();
                        RoomInfo info = snap.child("RoomInfo").getValue(RoomInfo.class);
                        ArrayList<String> ids = new ArrayList<>();

                        if(snap.child("RoomInfo").child("downloadURL").hasChildren()){
                            Iterator<DataSnapshot> iter2 = snap.child("RoomInfo").child("downloadURL").getChildren().iterator();
                            while (iter2.hasNext()){
                                DataSnapshot snap2 = iter2.next();
                                ids.add(snap2.getKey());
                                //Log.d("로그", "추가: " + snap2.getKey());
                            }
                        }

                        mRoomList.add(new DrawPerRoom(snap.getKey(), info.title, ids));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // 로그인된 유저가 그렸던 그림들의 리스트를 가져와 저장 후 보여줌
        mRef.child("user").child(mUser.getUid()).child("downloadURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDrawArrayList.clear();

                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()) {
                        DataSnapshot snap = iter.next();
                        DrawInfo info = snap.getValue(DrawInfo.class);
                        Log.d("로그", "key: " + snap.getKey());
                        mTitle = compare(snap.getKey());
                        Log.d("로그", "title: " + mTitle);
                        mDrawArrayList.add(new MyDraw(snap.getKey(), mTitle, info));
                    }
                }
                mAdapter.notifyDataSetChanged();
                mTvNumOfDraw.setText(String.valueOf(mDrawArrayList.size()).concat(" photos"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void OnClick(View view) {
        switch (view.getId()){
            case R.id.iv_my_draw_back_arrow:    // 뒤로가기 이미지 클릭 시 행동
                finish();
                break;
            case R.id.iv_my_draw_menu:          // 메뉴 이미지 클릭 시 행동
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.setOnMenuItemClickListener(this);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.my_draw_menu, popupMenu.getMenu());
                popupMenu.show();
                break;
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove:   // 삭제하기 메뉴
                if(!mRemove) {
                    mRemove = true;
                    mTvCheckRemove.setText(getString(R.string.my_draw_tv_check_remove));
                }else{
                    mRemove = false;
                    mTvCheckRemove.setText("");

                    //체크된 것 삭제 기능
                    removeDraw();

                    mAdapter.notifyDataSetChanged();
                }
                return true;
            case R.id.remove_all:   // 일괄삭제 메뉴
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.my_draw_menu_remove_all)).setMessage(R.string.my_draw_dialog_remove_all_content);
                builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(MyDraw draw : mDrawArrayList){
                            draw.isCheck = true;
                        }

                        removeDraw();
                    }
                }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            default:
                return false;
        }
    }

    // 그림을 지우는 함수
    public void removeDraw(){

        int cnt = 0;    // 지워진 그림의 개수
        for(MyDraw draw : mDrawArrayList){

            if(draw.isCheck){
                cnt += 1;
                String room = searchRoom(draw.id);

                // 방에서 선택된 그림의 정보 지우기
                DatabaseReference mRoomRemove = mFirebase.getReference().child("room").child("room_id").child(room).child("RoomInfo").child("downloadURL").child(draw.id);
                mRoomRemove.removeValue();

                // 유저 정보에서 선택된 그림의 정보 지우기
                DatabaseReference mRemoveRef = mFirebase.getReference().child("user").child(mUser.getUid()).child("downloadURL").child(draw.id);
                mRemoveRef.removeValue();

                // 파이어베이스 스토리지에 저장된 그림 삭제
                mStorage.getReference().child(room).child(draw.info.direction).child(mUser.getUid()).child(draw.info.fileName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("로그", "이미지 삭제 완료");
                    }
                });

                // 파이어베이스 스토리지에 저장된 썸네일 그림 삭제
                mStorage.getReference().child(room).child(draw.info.direction).child(mUser.getUid()).child(draw.info.fileName.concat("(thumnail)")).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("로그", "썸네일 삭제 완료");
                    }
                });

                // 그림이 있는 방 그림 개수 동기화
                updateRoomDoodleCount(room);
            }
        }

        // 모든 방에 있는 총 그림 개수의 합 동기화
        final int count = cnt;
        final DatabaseReference totalRef = mFirebase.getReference().child("totaldoodles");
        totalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long total = dataSnapshot.getValue(Long.class);
                totalRef.setValue(total - count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // 해당 아이디를 가진 방의 이름 리턴
    public String compare(String id){
        String result = "";
        for(int i=0; i < mRoomList.size(); i++){
            for(int j=0; j<mRoomList.get(i).ids.size(); j++){
                if(id.equals(mRoomList.get(i).ids.get(j))){
                    result = mRoomList.get(i).title;
                    return result;
                }
            }
        }

        return result;
    }

    // 해당 아이디를 가진 방 리턴
    public String searchRoom(String id){
        String result = "";

        for(int i=0; i<mRoomList.size(); i++){
            for(int j=0; j< mRoomList.get(i).ids.size(); j++){
                if(mRoomList.get(i).ids.get(j).equals(id)){
                    return mRoomList.get(i).room_id;
                }
            }
        }

        return result;
    }

    // 사용자가 그림을 지우면 사용자가 그린 그림의 개수 동기화
    public void updateRoomDoodleCount(String room_id){

        final ArrayList<String> roomDoodleList = new ArrayList<>();
        final DatabaseReference roomDoodleRef = mFirebase.getReference().child("room").child("room_id").child(room_id).child("RoomInfo");
        roomDoodleRef.child("downloadURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()){
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()){
                        DataSnapshot snap = iter.next();
                        String doodle = snap.getKey();
                        roomDoodleList.add(doodle);
                    }
                }
                roomDoodleRef.child("doodles").setValue(roomDoodleList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

}
