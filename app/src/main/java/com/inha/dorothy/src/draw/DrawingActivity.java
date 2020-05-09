package com.inha.dorothy.src.draw;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.R;
import com.inha.dorothy.src.draw.cameraset.Camera2Preview;
import com.inha.dorothy.src.draw.sensorset.SensorSet2;
import com.inha.dorothy.src.draw.view.AutoFitTextureView;
import com.inha.dorothy.src.draw.view.DrawingView;
import com.inha.dorothy.src.firebase.StorageSet;


import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class DrawingActivity extends BaseActivity implements View.OnClickListener {
    static String TAG = "DrawingAcitivty";

    //Floating Action Button
    private FloatingActionButton mSelectColorButton;

    //custom drawing view
    private DrawingView drawView;
    //buttons
    private ImageView currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
    //sizes
    private float smallBrush, mediumBrush, largeBrush;


    //Camera
    //private TextureView textureView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_drawing);

        mSelectColorButton = findViewById(R.id.fab_drawing_select_color);

//        doodleCount = 0;

//        mAuth = FirebaseAuth.getInstance();

        imageViewFrame = findViewById(R.id.imageViewFrame);
        progressDoodles = findViewById(R.id.progress_doodles);

//        if(!MainActivity.placeName.isEmpty()) placeName = MainActivity.placeName;
//        Log.d("QRcode", "Draw-placeName : " +placeName);
//        storageSet = new_pic StorageSet(this, placeName);

        //get drawing view
        drawView = findViewById(R.id.drawing);


        //sizes from dimensions
//        smallBrush = getResources().getInteger(R.integer.small_size);
//        mediumBrush = getResources().getInteger(R.integer.medium_size);
//        largeBrush = getResources().getInteger(R.integer.large_size);
        smallBrush =10;
        mediumBrush = 20;
        largeBrush = 30;
        //draw button
        drawBtn =  findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //set initial size
        drawView.setBrushSize(smallBrush);

        //erase button
        eraseBtn =  findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);

        //new_pic button
        newBtn =  findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //save button
        saveBtn =  findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

            //API21이상
            textureView =  findViewById(R.id.textureView);
            camera2Preview = new Camera2Preview(this, textureView);


        sensorSet2 = new SensorSet2(this);


//다운로드Receiver

//        mDownloadReceiver = new_pic BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(TAG, "downloadReceiver:onReceive:" + intent);
//                hideProgressDialog();
//
//                if (DownloadService.ACTION_COMPLETED.equals(intent.getAction())) {
//
//                    sensorSet2.makeValueFromFileName(intent.getStringExtra(DownloadService.EXTRA_FILE_NAME)
//                            , intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH), false);
//                    doodleCount++;
//                    progressDoodles.setText("Download : "+doodleCount+"\nTotal : "+storageSet.getUrls().size());
//
//
//                    downloadCheck = true;
//                    hideProgressDialog();
//                }
//
//                if (DownloadService.ACTION_ERROR.equals(intent.getAction())) {
//                    String path = intent.getStringExtra(DownloadService.EXTRA_DOWNLOAD_PATH);
//                    downloadCheck = false;
//                    hideProgressDialog();
//                    Log.e(TAG, "download fail path" + path);
//                }
//            }
//        };


    }

    @Override
    public void onStart() {
        super.onStart();
//
//        // Register download receiver
//        LocalBroadcastManager.getInstance(this)
//                .registerReceiver(mDownloadReceiver, DownloadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadReceiver);
//        Log.d(TAG, "Cachedir : "+getCacheDir());
//        getCacheDir().deleteOnExit();

    }

    @Override
    protected void onPause() {
        super.onPause();
        camera2Preview.onPause();
        Log.d("SDK", "SDK version 21+: " + Build.VERSION.SDK_INT);
        //        storageSet.onPause();
//        sensorSet2.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera2Preview.onResume();
        Log.d("SDK", "SDK version 21+: " + Build.VERSION.SDK_INT);
//        storageSet.onResume(); 원격 저장소 Resume
//        sensorSet2.onResume();
    }

    /**
     * API 21+에서 카메라 사용을 승인했을 때 다시 카메라뷰를 띄우기위해
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            textureView = (AutoFitTextureView) findViewById(R.id.textureView);
                            camera2Preview = new Camera2Preview(this, textureView);
                            camera2Preview.openCamera(textureView.getWidth(), textureView.getHeight());
                            Log.d(TAG,"mPreview set");
                        } else {
                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }



    //download check
//    public void setDownloadCheck(boolean check){ downloadCheck = check; }
//    public boolean getDownloadCheck(){ return downloadCheck; }

//    public SensorSet2 getSensorSet2(){ return sensorSet2;}
//
//    public void setProgressDoodles(){
//        progressDoodles.setText("Download : "+ ++doodleCount+"\nTotal : "+ storageSet.getUrls().size());
//    }




    //그리기 도구 버튼들
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.draw_btn :
                //draw button clicked
                final Dialog brushDialog = new Dialog(this);
                brushDialog.setTitle("Brush size:");
                brushDialog.setContentView(R.layout.brush_chooser);
                //listen for clicks on size buttons
                ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(smallBrush);
                        drawView.setLastBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawView.setErase(false);
                        drawView.setBrushSize(mediumBrush);
                        drawView.setLastBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });
                ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
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
            case R.id.erase_btn :
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

            case R.id.new_btn :
                //new_pic button
                AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new_pic drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        drawView.startNew();
                        dialog.dismiss();
                        //imageView.setImageResource(android.R.color.transparent);
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newDialog.show();
                break;
//            case saveBtn :
//                //save drawing
//                AlertDialog.Builder saveDialog = new_pic AlertDialog.Builder(this);
//                saveDialog.setTitle("낙서남기기");
//                saveDialog.setMessage(TranslationUtil.transPlaceNameENtoKOR(placeName)
//                        +"\n"+ DateFormat.getDateTimeInstance().format(new_pic Date())
//                        +"\n가로(Azimuth): "+ sensorSet2.getSensorAzimuth()+"°["+sensorSet2.getSensorDirection()+"]"
//                        +"\n세로(Pitch) : "+ sensorSet2.getSensorPitch()
//                        +"\n이곳에 낙서를 남길까요?");
//                saveDialog.setPositiveButton("네", new_pic DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        //save drawing
//                        drawView.setDrawingCacheEnabled(true);
//
//                        //firebase에 저장
//                        storageSet.uploadFromMemory(drawView, mAuth.getCurrentUser().getUid()
//                                , sensorSet2.getSensorDirection(), sensorSet2.getSensorAzimuth()
//                                ,sensorSet2.getSensorPitch(), sensorSet2.getSensorRoll());
//
//                        drawView.destroyDrawingCache();
//                        drawView.startNew();
//                    }
//                });
//                saveDialog.setNegativeButton("아니오", new_pic DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//                saveDialog.show();
//                break;

            case R.id.fab_drawing_select_color :
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
