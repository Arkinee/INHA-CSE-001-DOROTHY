package com.inha.dorothy.src.draw;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;
import com.inha.dorothy.src.draw.cameraset.Camera2Preview;
import com.inha.dorothy.src.draw.sensorset.SensorSet2;
import com.inha.dorothy.src.draw.view.AutoFitTextureView;
import com.inha.dorothy.src.draw.view.DrawingView;
import com.inha.dorothy.src.firebase.DownloadService;
import com.inha.dorothy.src.firebase.StorageSet;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import petrov.kristiyan.colorpicker.ColorPicker;

public class DrawingActivity extends BaseActivity implements View.OnClickListener {
    static String TAG = "DrawingAcitivty";

    //custom drawing view
    private DrawingView drawView;
    //sizes
    private float smallBrush, mediumBrush, largeBrush;
    //Camera
    private AutoFitTextureView textureView;
    //Camera2preview
    private Camera2Preview camera2Preview;
    public static final int REQUEST_CAMERA = 1;

    //저장된 이미지 불러올 뷰
    public FrameLayout imageViewFrame;

    //센서
    private SensorSet2 sensorSet2;

    //다운받은 낙서 수 / 총 낙서수
    private TextView progressDoodles;
    private int doodleCount;

    //firebase
    private StorageSet storageSet;
    private FirebaseAuth mAuth;
    private BroadcastReceiver mDownloadReceiver;
    private boolean downloadCheck = false;

    //방의 firebase key값(room_id)
    private String roomId;
    //뒤로가기 입력 시간 저장 변수
    private long backBtnTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_drawing);

        doodleCount = 0;
        mAuth = FirebaseAuth.getInstance();

        //get drawing view
        drawView = findViewById(R.id.drawing);

        //다운 받은 이미지를 보여주는 imageViewFrame
        imageViewFrame = findViewById(R.id.imageViewFrame);
        progressDoodles = findViewById(R.id.progress_doodles);

        roomId = getIntent().getStringExtra("room_id");
        storageSet = new StorageSet(this, roomId);


//        sizes from dimensions
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        //set initial size
        drawView.setBrushSize(smallBrush);

        //API21이상
        textureView = findViewById(R.id.textureView);
        camera2Preview = new Camera2Preview(this, textureView);
        sensorSet2 = new SensorSet2(this);


        //다운로드Receiver
        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "downloadReceiver:onReceive:" + intent);
                hideProgressDialog();

                if (DownloadService.ACTION_COMPLETED.equals(intent.getAction())) {

                    sensorSet2.makeValueFromFileName(Objects.requireNonNull(intent.getStringExtra(DownloadService.EXTRA_FILE_NAME))
                            , intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH), false);
                    doodleCount++;
                    progressDoodles.setText("Download : "+doodleCount+"\nTotal : "+storageSet.getmUrls().size());


                    downloadCheck = true;
                    hideProgressDialog();
                }

                if (DownloadService.ACTION_ERROR.equals(intent.getAction())) {
                    String path = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH);
                    downloadCheck = false;
                    hideProgressDialog();
                    Log.e(TAG, "download fail path" + path);
                }
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
//
        // Register download receiver
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDownloadReceiver, DownloadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadReceiver);
        Log.d(TAG, "Cachedir : "+getCacheDir());
        getCacheDir().deleteOnExit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("SDK", "SDK version 21+: " + Build.VERSION.SDK_INT);
        storageSet.onPause();
        sensorSet2.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera2Preview.onResume();
        Log.d("SDK", "SDK version 21+: " + Build.VERSION.SDK_INT);
        storageSet.onResume();// 원격 저장소 Resume
        sensorSet2.onResume();
    }

    /**
     * API 21+에서 카메라 사용을 승인했을 때 다시 카메라뷰를 띄우기위해
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            textureView = (AutoFitTextureView) findViewById(R.id.textureView);
                            camera2Preview = new Camera2Preview(this, textureView);
                            camera2Preview.openCamera(textureView.getWidth(), textureView.getHeight());
                            Log.d(TAG, "mPreview set");
                        } else {
                            Toast.makeText(this, "Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }


    //download check
    public void setDownloadCheck(boolean check){ downloadCheck = check; }
    public boolean getDownloadCheck(){ return downloadCheck; }
    public SensorSet2 getSensorSet2(){ return sensorSet2;}
    public void setProgressDoodles(){
        progressDoodles.setText("Download : "+ ++doodleCount+"\nTotal : "+ storageSet.getmUrls().size());
    }


    //뷰 사이즈의 bitmap을 만들고 해당 bitmap을 객체로 갖는 Canvas를 만들어
    // View의 내용을 Canvas에 넣어 bitmap을 얻어 뷰의 이미지를 얻는 방법이다.
    public Bitmap getBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }


    //2번 누르면 뒤로가기 Custom
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;

        if(0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        }
        else {
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }


    }


    //그리기 도구 버튼들
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.draw_btn:
                //draw button clicked
                final Dialog brushDialog = new Dialog(this);
                brushDialog.setTitle("Brush size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                //listen for clicks on size buttons
                ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(smallBrush);
                        drawView.setLastBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(mediumBrush);
                        drawView.setLastBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(largeBrush);
                        drawView.setLastBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });
                //show and wait for user interaction
                brushDialog.show();
                break;

            case R.id.erase_btn:
                //switch to erase - choose size
                final Dialog e_brushDialog = new Dialog(this);
                e_brushDialog.setTitle("Eraser size:");
                e_brushDialog.setContentView(R.layout.brush_chooser);
                //size buttons
                smallBtn = e_brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(true);
                        drawView.setBrushSize(smallBrush);
                        e_brushDialog.dismiss();
                    }
                });
                mediumBtn = (ImageButton) e_brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(true);
                        drawView.setBrushSize(mediumBrush);
                        e_brushDialog.dismiss();
                    }
                });
                largeBtn = (ImageButton) e_brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(true);
                        drawView.setBrushSize(largeBrush);
                        e_brushDialog.dismiss();
                    }
                });
                e_brushDialog.show();
                break;
            case R.id.new_btn:
                //new_pic button
                AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new_pic drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        drawView.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newDialog.show();
                break;
            case R.id.save_btn:
                //save drawing
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                saveDialog.setTitle("낙서남기기");
                saveDialog.setMessage(DateFormat.getDateTimeInstance().format(new Date())
                        + "\n가로(Azimuth): " + sensorSet2.getSensorAzimuth() + "°[" + sensorSet2.getSensorDirection() + "]"
                        + "\n세로(Pitch) : " + sensorSet2.getSensorPitch()
                        + "\n이곳에 낙서를 남길까요?");
                saveDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //drawView 객체를 Bitmap 형식으로 전환 후 전달
                        //20-05-12 summer write
                        Bitmap bitmap = getBitmapFromView(drawView);
                        //firebase에 저장하기 Bitmap 형식의 캡쳐된 View, 구글의 userId 고유값, 방향, Azimuth,pitch,roll 전달
                        storageSet.uploadFromMemory(bitmap, mAuth.getCurrentUser().getUid()
                                , sensorSet2.getSensorDirection(), sensorSet2.getSensorAzimuth()
                                , sensorSet2.getSensorPitch(), sensorSet2.getSensorRoll());

                        drawView.startNew();
                    }
                });
                saveDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                saveDialog.show();
                break;

            case R.id.fab_drawing_select_color:
                final ColorPicker colorPicker = new ColorPicker(DrawingActivity.this);
                ArrayList<String> colors = new ArrayList<>();
                colors.add("#ffffff");
                colors.add("#dfdde0");
                colors.add("#c1c2c4");
                colors.add("#818286");
                colors.add("#000000");
                colors.add("#7d4417");
                colors.add("#69481f");
                colors.add("#ac927b");
                colors.add("#b7ab9d");
                colors.add("#f3e6d5");
                colors.add("#8f539d");
                colors.add("#c780c6");
                colors.add("#a59dd0");
                colors.add("#0a2f49");
                colors.add("#3354b3");
                colors.add("#acdee5");
                colors.add("#92ceb5");
                colors.add("#35abad");
                colors.add("#3588b4");
                colors.add("#5b99fe");
                colors.add("#ffe14f");
                colors.add("#ffdfa2");
                colors.add("#ced184");
                colors.add("#5aa352");
                colors.add("#21633d");
                colors.add("#f7b932");
                colors.add("#f07b2c");
                colors.add("#fe8664");
                colors.add("#e96561");
                colors.add("#ce4646");

                colorPicker
                        .setDefaultColorButton(Color.parseColor("#f84c44"))
                        .setColors(colors)
                        .setColumns(5)
                        .setRoundColorButton(true)
                        .setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                            @Override
                            public void onChooseColor(int position, int color) {
                                Log.d("position", "" + position);// will be fired only when OK button was tapped
                                drawView.setPaintColor(color);
                            }

                            @Override
                            public void onCancel() {

                            }
                        }).show();

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        } // end Switch

    }//end on Click
}
