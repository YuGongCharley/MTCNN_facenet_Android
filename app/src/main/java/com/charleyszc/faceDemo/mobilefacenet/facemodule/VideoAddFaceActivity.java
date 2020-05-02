package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.charleyszc.faceDemo.mobilefacenet.R;
import com.charleyszc.faceDemo.mobilefacenet.uvc.PreviewImage;
import com.example.lyf.yflibrary.Permission;
import com.example.lyf.yflibrary.PermissionResult;

import java.io.File;
import java.io.IOException;

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.recognize.verifyStoragePermissions;


/**
 * Created by szc on 2019/07/04
 */
public class VideoAddFaceActivity extends AppCompatActivity {

    static final String TAG = "VideoAddFaceActivity";

    Context context = VideoAddFaceActivity.this;
    private String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

//    private SurfaceView surfaceView; //surfaceview对象：（视图组件）视频显示
//    CameraSurfaceHolder mCameraSurfaceHolder = new CameraSurfaceHolder();

    private ImageView mLiveImage = null;
    private static Handler handler = null;
    private SurfaceHolder holder1, holder0;
    private SurfaceView surface0, surface1;
    private Camera camera1, camera0;
    private int hwColorCamera = 1; //方便切换摄像头 1
    private int hwRedCamera = 0; //方便切换摄像头 0
    private int displayOrientColor = 90;//数据旋转 90
    private int displayOrientRed = 270;//数据旋转 270
    private int displayOrient = 270; //预览旋转
    private int mWidth = 800; //值只能在摄像头支持的范围选择800X600 1280X720
    private int mHeight = 600;//值越大，识别距离越远，耗时越大

    Button btnTakephone;//拍照检验原图情况
    EditText edtColor_orient,edtRed_orient,edtDsp_orient;
    String sdPath=null;

    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
//    private View previewView;

    public void setContext(Context context) {
        this.context = context;
    }

    //TODO: onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: 确认权限
        verifyStoragePermissions(VideoAddFaceActivity.this);

        //TODO: model init 初始化 人脸模型
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        sdPath = sdDir.toString() + "/facem/";

        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                break;
        }
//        previewView = findViewById(R.id.texture_preview);
        Permission.checkPermisson(this, REQUEST_PERMISSIONS, new PermissionResult() {
            @Override
            public void success() {
                //成功
                // 设置相机监听
                Log.i("Permission","Permission Success");
//                surface1 = findViewById(R.id.surface_1);

//                startCamera1();
//                initView();
            }

            @Override
            public void fail() {
                //失败
                Log.i("Permission","Permission Failed");
            }
        });

        surface0 = findViewById(R.id.video_add_face_view);
        startCamera0();


    }
//    public void initView()
//    {
//        setContentView(R.layout.activity_videoaddface);
//        surfaceView = (SurfaceView) findViewById(R.id.videoAddFaceView);
//        mCameraSurfaceHolder.setAddCameraSurfaceHolder(context,surfaceView);
//
//    }

    /**
     * 开启第一个Camera
     */
    private void startCamera0() {
        holder0 = surface0.getHolder();
        holder0.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                camera0 = Camera.open(hwColorCamera);
                Camera.Parameters parameter = camera0.getParameters();
                parameter.setPictureSize(mWidth,mHeight);
                parameter.setPreviewSize(mWidth,mHeight);
                camera0.setParameters(parameter);
                try {
                    //图像显示旋转
                    camera0.setDisplayOrientation((hwColorCamera*180+displayOrient)%360);
                    camera0.setPreviewDisplay(holder0);

                    camera0.setPreviewCallback(new Camera.PreviewCallback() {
                        private int mPreviewCount = 0;
                        //实际数据旋转
                        private int mPreviewOrient = displayOrientColor;
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            Camera.Size s = camera.getParameters().getPreviewSize();
                            mPreviewCount = (mPreviewCount >=Integer.MAX_VALUE?0:mPreviewCount+1);
                            if (mPreviewCount % 10 == 0) {
                                //临时存储给检测线程调用
                                PreviewImage.setColorData(data, s.width, s.height, PreviewImage.DATA_TYPE_YUV, mPreviewOrient);
                            }

                        }
                    });
                    camera0.startPreview();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (null != camera0) {
                    camera0.stopPreview();
                    camera0.release();
                    camera0 = null;
                }
            }
        });
        holder0.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * 开启第二个Camera
     */
    private void startCamera1() {
        holder1 = surface1.getHolder();
        holder1.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                camera1 = Camera.open(hwRedCamera);
                Camera.Parameters parameter = camera1.getParameters();
                parameter.setPictureSize(mWidth,mHeight);
                parameter.setPreviewSize(mWidth,mHeight);
                camera1.setParameters(parameter);
                try {
                    //显示设置旋转角度
                    camera1.setDisplayOrientation((hwRedCamera*180+displayOrient)%360);
                    camera1.setPreviewDisplay(holder1);
                    camera1.setPreviewCallback(new Camera.PreviewCallback() {
                        private int mPreviewCount = 0;
                        //图像实际数据旋转
                        private int mPreviewOrient = displayOrientRed;
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            Camera.Size s = camera.getParameters().getPreviewSize();
                            mPreviewCount = (mPreviewCount >=Integer.MAX_VALUE?0:mPreviewCount+1);
                            if (mPreviewCount % 10 == 0) {
                                //临时存储给检测线程调用
                                PreviewImage.setRedData(data, s.width, s.height, PreviewImage.DATA_TYPE_YUV, mPreviewOrient);
                            }
                        }
                    });
                    camera1.startPreview();//开始预览
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (null != camera1) {
                    camera1.stopPreview();
                    camera1.release();
                    camera1 = null;
                }
            }
        });
        //添加回调
        holder1.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

//    //保存参数
//    private void initData() {
//        //拍照
//        btnTakephone = (Button) findViewById(R.id.takephone);
//        btnTakephone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreviewImage colorData = PreviewImage.getColorData();
//                Bitmap bmp = ConvertUtils.yuvToBitmapRotate(colorData.mData, colorData.mWidth, colorData.mHeight,ConvertUtils.ROTATE_TYPE_90);
//                saveImage(bmp);
//                PreviewImage redData = PreviewImage.getRedData();
//                Bitmap bmpRed = ConvertUtils.yuvToBitmapRotate(redData.mData, redData.mWidth, redData.mHeight,ConvertUtils.ROTATE_TYPE_90);
//                saveBitmap(bmpRed, String.valueOf(System.currentTimeMillis()));
//            }
//        });

        //切换摄像头
//        Button changecamera = (Button) findViewById(R.id.change);
//        changecamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPreferences.Editor editor;
//                editor = getApplicationContext().getSharedPreferences("zkas", MODE_WORLD_WRITEABLE).edit();
//                int nTemp = hwColorCamera;
//                hwColorCamera = hwRedCamera;
//                hwRedCamera = nTemp;
//
//                editor.putInt("hwColorCamera", hwColorCamera);
//                editor.putInt("hwRedCamera", hwRedCamera);
//
//                int i1 = Integer.parseInt(edtColor_orient.getText().toString());
//                int i2 = Integer.parseInt(edtRed_orient.getText().toString());
//                int i3 = Integer.parseInt(edtDsp_orient.getText().toString());
//                editor.putInt("displayOrientColor",i1);
//                editor.putInt("displayOrientRed",i2);
//                editor.putInt("displayOrient",i3);
//                editor.commit();
//                finish();
//            }
//        });


//    }

    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera0.stopPreview();
//        camera1.stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
