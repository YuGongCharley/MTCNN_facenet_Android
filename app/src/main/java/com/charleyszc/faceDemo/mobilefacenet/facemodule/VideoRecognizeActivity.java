package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.charleyszc.faceDemo.mobilefacenet.R;
import com.charleyszc.faceDemo.mobilefacenet.frontcamera.CameraSurfaceHolder;
import com.example.lyf.yflibrary.Permission;
import com.example.lyf.yflibrary.PermissionResult;

import java.io.File;

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.compare.verifyStoragePermissions;

/**
 * Created by szc on 2019/05/14
 */
public class VideoRecognizeActivity extends AppCompatActivity  {
    static final String TAG = "VideoRecognizeActivity";

    Context context = VideoRecognizeActivity.this;
    private SurfaceView surfaceView; //surfaceview对象：（视图组件）视频显示
    private String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    CameraSurfaceHolder mCameraSurfaceHolder = new CameraSurfaceHolder();
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
        verifyStoragePermissions(VideoRecognizeActivity.this);

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

                initView();
            }

            @Override
            public void fail() {
                //失败
                Log.i("Permission","Permission Failed");
            }
        });


    }
    public void initView()
    {
        setContentView(R.layout.activity_recognize);
        surfaceView = (SurfaceView) findViewById(R.id.recognizeFaceView);
        mCameraSurfaceHolder.setRecCameraSurfaceHolder(context,surfaceView);

    }

//    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
//        if (previewing) {
//            mCamera.stopPreview();
//            Log.i(TAG, "停止预览");
//        }
//
//        try {
//            mCamera.setPreviewDisplay(arg0);
//            mCamera.startPreview();
//            mCamera.setPreviewCallback(this);
//            Log.i(TAG, "开始预览");
//            //调用旋转屏幕时自适应
//            setCameraDisplayOrientation((Activity) context, FrontCamera.mCurrentCamIndex, mCamera);
//        } catch (Exception e) {
//        }
//    }
//
//    public void surfaceCreated(SurfaceHolder holder) {
//        //初始化前置摄像头
//        mFrontCamera.setCamera(mCamera);
//        mCamera = mFrontCamera.initCamera();
//        mCamera.setPreviewCallback(this);
//        //适配竖排固定角度
//        Log.i(TAG, "context: " + context.toString());
//        Log.i(TAG, "mFrontCamera: " + mFrontCamera.toString());
//        Log.i(TAG, "mCamera: " + mCamera.toString());
//        setCameraDisplayOrientation((Activity) context, mFrontCamera.getCurrentCamIndex(), mCamera);
//    }
//
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        mFrontCamera.StopCamera(mCamera);
//    }
//
//    /**
//     * 相机实时数据的回调
//     *
//     * @param data   相机获取的数据，格式是YUV
//     * @param camera 相应相机的对象
//     */
//    @Override
//    public void onPreviewFrame(byte[] data, Camera camera) {
//        if (mRecognizeTask != null) {
//            switch (mRecognizeTask.getStatus()) {
//                case RUNNING:
//                    return;
//                case PENDING:
//                    mRecognizeTask.cancel(false);
//                    break;
//            }
//
//        }
//
//        mRecognizeTask = new FaceRecognizeTask(data, camera);
////        mRecognizeTask.execute((Void) null);
//        //Log.i(TAG, "onPreviewFrame: 启动了Task");
//
//    }


}
