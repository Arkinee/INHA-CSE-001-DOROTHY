package com.inha.dorothy.src.mydraw;

import android.app.AlertDialog;
import android.content.Context;
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
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private ArrayList<MyDraw> mDrawArrayList;
    private ArrayList<DrawPerRoom> mRoomList;
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

        RecyclerView rvMyDraw = findViewById(R.id.rv_my_draw);
        rvMyDraw.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        rvMyDraw.addItemDecoration(new ItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing),
                getResources().getInteger(R.integer.draw_list_columns)));

        mTvNumOfDraw = findViewById(R.id.tv_my_draw_number);
        mTvCheckRemove = findViewById(R.id.tv_my_draw_remove);

        mDrawArrayList = new ArrayList<>();
        mRoomList = new ArrayList<>();
        mRemove = false;
        mContext = this;

        mAdapter = new DrawAdapter(this, mDrawArrayList);
        rvMyDraw.setAdapter(mAdapter);

        accessDatabase();

        //그림 클릭 이벤트
        mAdapter.setOnItemClickListener(new DrawAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                if(!mRemove){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getString(R.string.my_draw_dialog_title)).setMessage(mDrawArrayList.get(pos).room);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else{
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

    }

    void accessDatabase() {
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
                                Log.d("로그", "추가: " + snap2.getKey());
                            }
                        }

                        mRoomList.add(new DrawPerRoom(snap.getKey(), info.title, ids));
                        Log.d("로그", "리스트에 추가: " + snap.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
            case R.id.iv_my_draw_back_arrow:
                finish();
                break;
            case R.id.iv_my_draw_menu:
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
            case R.id.remove:
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
            case R.id.remove_all:
                showCustomMessage("방 삭제");
                return true;
            default:
                return false;
        }
    }

    public void removeDraw(){

        for(MyDraw draw : mDrawArrayList){

            if(draw.isCheck){
                Log.d("로그", "id: " + draw.id);
                String room = searchRoom(draw.id);

                mStorage.getReference().child(room).child(draw.info.direction).child(mUser.getUid()).child(draw.info.fileName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("로그", "이미지 삭제 완료");
                    }
                });

                mStorage.getReference().child(room).child(draw.info.direction).child(mUser.getUid()).child(draw.info.fileName.concat("(thumnail)")).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("로그", "썸네일 삭제 완료");
                    }
                });

                DatabaseReference mRoomRemove = mFirebase.getReference().child("room").child("room_id").child(room).child("RoomInfo").child("downloadURL").child(draw.id);
                mRoomRemove.removeValue();

                DatabaseReference mRemoveRef = mFirebase.getReference().child("user").child(mUser.getUid()).child("downloadURL").child(draw.id);
                mRemoveRef.removeValue();
            }
        }
    }

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

}
