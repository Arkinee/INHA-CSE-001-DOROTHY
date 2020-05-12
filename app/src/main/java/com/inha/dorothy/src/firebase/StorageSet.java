package com.inha.dorothy.src.firebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inha.dorothy.src.draw.DrawingActivity;
import com.inha.dorothy.src.draw.sensorset.SensorSet2;
import com.inha.dorothy.src.firebase.model.DownloadImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StorageSet {

    private static final String TAG = "StorageSet";
    private DrawingActivity mDrawingActivity;

    //Auth
    private FirebaseAuth mAuth;

    //DB
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mRoomDownloadUrl;
    private DatabaseReference mRoomDoodles;
    private DatabaseReference mTotalDoodles;

    private ValueEventListener mRoomValueEventListener;

    // Create a storage reference from our app
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReferenceFromUrl("gs://inha-cse-dorothy.appspot.com/");

    private ProgressDialog mProgressDialog;

    private String mRoomId;
    private Uri mDownloadUrl = null;
    private Uri mThumnailUrl = null;


    private static ArrayList<DownloadImage> mUrls;

    public StorageSet(final DrawingActivity activity, final String roomId) {
        this.mDrawingActivity = activity;
        this.mRoomId = roomId;
        mAuth = FirebaseAuth.getInstance();
        mUrls = new ArrayList<>();

        //업로드를 위한 레퍼런스
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("user").child(mAuth.getCurrentUser().getUid());
        mRoomDownloadUrl = FirebaseDatabase.getInstance().getReference().child("room").child("room_id").child(roomId).child("RoomInfo").child("downloadURL");
        mRoomDoodles = FirebaseDatabase.getInstance().getReference().child("room").child("room_id").child(roomId).child("RoomInfo").child("doodles");
        mTotalDoodles = FirebaseDatabase.getInstance().getReference().child("totaldoodles");

        // 해당 엑티비티 내에서 DB변경이 있을 경우 실시간 변경
        mRoomValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        mUrls.add(snapshot.getValue(DownloadImage.class));
                    }

                } catch (Exception e) {
                    Log.d(TAG, "에러" + e);
                }
                try {
                    if (!mUrls.isEmpty()) {
                        activity.showProgressDialog();
                        Intent intent = new Intent(activity, DownloadService.class);
                        intent.setAction(DownloadService.ACTION_DOWNLOAD);
                        activity.startService(intent);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "downloadurl : 에러" + e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(activity, "DB Error", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void onPause() {
        mUrls.clear();
        if (mRoomValueEventListener != null)
            mRoomDownloadUrl.removeEventListener(mRoomValueEventListener);
    }

    public void onResume() {
        mRoomDownloadUrl.addListenerForSingleValueEvent(mRoomValueEventListener);
    }


    /**
     * @param drawView
     * @param userID
     * @param direction
     * @param azimuth
     * @param pitch
     * @param roll      storage에 Bitmap을 업로드하는 과정
     **/
    public void uploadFromMemory(Bitmap drawView, final String userID
            , final String direction, final int azimuth, final int pitch, final int roll) {

        showProgressDialog("Please, Wait...");

        //drawView의 캡쳐된 화면을 받아 360,640 크기로 변환 하여 저장)
        Bitmap bitmap = Bitmap.createScaledBitmap(drawView, 360, 640, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //100% 품질로 PNG로 압축 하여 압축된 byte 배열을 baos에 넣는다.
        //convert stream to byte array
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        final byte[] data = baos.toByteArray();

        //썸네일용 drawView의 캡쳐된 화면을 받아 크기 변환 후 저장
        bitmap = Bitmap.createScaledBitmap(bitmap, 198, 198, true);
        ByteArrayOutputStream tumbnailBaos = new ByteArrayOutputStream();

        //80% 품질로 PNG로 압축 하여 압축된 byte 배열을 baos에 넣는다.
        //convert stream to byte array
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, tumbnailBaos);
        byte[] thumbnail = tumbnailBaos.toByteArray();


        Date date = new Date();
        final String createdTime = DateFormat.getDateTimeInstance().format(date);
        String rawTime = "" + date.getTime();
        final String fileName = azimuth + "," + pitch + "," + rawTime;

        //사진 스토리지의 저장 위치는 rooTitle.방향.userID.파일이름 즉 유저아이디 별 파일이 여러개 있다는 의미다.
        final StorageReference doodleRef = storageRef.child(mRoomId).child(direction).child(userID)
                .child(fileName);
        //썸네일도 동시에 저장한다.
        final StorageReference thumnailRef = storageRef.child(mRoomId).child(direction).child(userID)
                .child(fileName + "(thumnail)");


        /** Create file metadata including the content type
         * 메타데이터가 만약 설정되어있지 않다면,
         * 기본 값은 octet-stream 으로 설정되는데 이름 그대로 8비트 바이너리 배열을 의미하며
         * http나 이메일상에서 application 형식이 지정되지 않았거나 형식을 모를때 사용한다.
         * octet-stream 으로 MIME 타입이 지정된 경우 단지 바이너리 데이터로서 다운로드만 가능하게 처리하게 됩니다.
         * 따라서 image/png 를 통해 contentType은 png 파일이라는 것을 선언하는 것이다.
         **/
        final StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .setCustomMetadata("userID", userID)
                .setCustomMetadata("time", rawTime)
                .setCustomMetadata("room", mRoomId)
                .setCustomMetadata("direction", direction)
                .setCustomMetadata("azimuth", "" + azimuth)
                .setCustomMetadata("pitch", "" + pitch)
                .setCustomMetadata("roll", "" + roll)
                .build();

        //썸네일 스토리지에 먼저 업로드한다.
        UploadTask thumnailUploadTask = thumnailRef.putBytes(thumbnail, metadata);
        thumnailUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mThumnailUrl = null;
                hideProgressDialog();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //업로드한 썸네일 다운로드 경로..
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while (!uri.isComplete()) ;
                mThumnailUrl = uri.getResult();
                //썸네일 업로드 성공 후 Url까지 받아왔다면 기존 DrawingView 이미지도 업로드
                UploadTask uploadTask = doodleRef.putBytes(data, metadata);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                       //썸네일 업로드 실패시
                        mDownloadUrl = null;
                        hideProgressDialog();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        //업로드한 그림 다운로드 경로받기
                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uri.isComplete()) ;
                        mDownloadUrl = uri.getResult();

                        //downloadURL 하위 푸시키 얻기
                        String urlKey = mDatabaseUser.child("downloadURL").push().getKey();

                        // users / UID /
                        // 해당 유저 DB에 낙서 수 +1 , download URL
                        DownloadImage dUrl = new DownloadImage(createdTime,mDownloadUrl.toString(), direction, mThumnailUrl.toString(), fileName, azimuth, pitch, roll);
                        mDatabaseUser.child("downloadURL").child(urlKey).setValue(dUrl);
                        mDatabaseUser.child("doodles").runTransaction(new Transaction.Handler() {
                            Integer myDoodles;

                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                    myDoodles = mutableData.getValue(Integer.class);
                                    if (myDoodles == null) {
                                        mutableData.setValue(1);
                                    } else {
                                        mutableData.setValue(myDoodles + 1);
                                    }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                //Complete
                            }
                        });

                        //DB의 각 방의 낙서 수 RoomInfo의 Doodles에 낙서 수 + 1
                        mRoomDownloadUrl.child(urlKey).setValue(dUrl);
                        mRoomDoodles.runTransaction(new Transaction.Handler() {
                            Integer roomDoodles;

                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {

                                    roomDoodles = mutableData.getValue(Integer.class);
                                if (roomDoodles == null) {
                                    mutableData.setValue(1);
                                } else {
                                    mutableData.setValue(roomDoodles + 1);
                                }

                                //if(roomDoodles == 0) return Transaction.success(mutableData); 이 부분은 제거해도 될 것 같다.
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                //Complete
                            }
                        });

                        //DB root의 TotalDoodles + 1
                        mTotalDoodles.runTransaction(new Transaction.Handler() {
                            Integer totalDoodles;

                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                    totalDoodles = mutableData.getValue(Integer.class);
                                    if (totalDoodles==null){
                                        mutableData.setValue(1);
                                    }else{
                                        mutableData.setValue(totalDoodles+1);
                                    }
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                // Transaction completed
                            }
                        });

                        //urlList에 현재 다운로드 url 추가
                        mUrls.add(dUrl);
                        //Local로 미리 다운로드 받는 기능
                        downloadToLocal(dUrl);
                        hideProgressDialog();
                        Toast.makeText(mDrawingActivity, "업로드 성공!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /** AR카메라 상태에서 그림을 올렸을 때 바로 동작하기 위한 UI스레드 다운로드
     *  그외 처음은 백그라운드서비스 다운로드
     *  save된 파일을 스토리지에 업로드 후 다시 다운받지는 않기에 local로 받아서 카메라 상태에서 바로 동작하게 한 기능
     *  **/


    /**
     * 로컬파일로 받아오기
     **/
    private void downloadToLocal(final DownloadImage url) {

        StorageReference islandRef = storage.getReferenceFromUrl(url.getUrl());
        try {
            // File name과 png 파일을 만드는 것
            // 디폴트 이름을 생성하기 위한 prefix와 suffix 문자열을 이용하여, 임시 디렉토리(default temporary-file directory)에
            // 새로운 임시(temp) 파일을 생성합니다.
            final File localFile = File.createTempFile(url.getFileName(), "png");

            //firebase -> local로 파일 다운로드
            islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Download", "다운로드받기성공");

                    //todo 20-05-13 주석 달기
                    mDrawingActivity.getSensorSet2().makeValueFromFileName(url.getFileName(), localFile.getAbsolutePath(), true);
                    mDrawingActivity.getSensorSet2().limitImageList(SensorSet2.LIMITED_CONCURRENT_IMAGE_VISIBILITY_COUNT, localFile.getAbsolutePath(), mUrls.size()-1);
                    mDrawingActivity.setProgressDoodles();

                    //로컬 임시 파일 삭제
                    localFile.deleteOnExit();
                    mDrawingActivity.setDownloadCheck(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mDrawingActivity.setDownloadCheck(false);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<DownloadImage> getmUrls() {
        return mUrls;
    }

    private void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mDrawingActivity);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}
