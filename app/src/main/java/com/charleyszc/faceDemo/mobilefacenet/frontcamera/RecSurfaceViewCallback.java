package com.charleyszc.faceDemo.mobilefacenet.frontcamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import static com.charleyszc.faceDemo.mobilefacenet.frontcamera.FrontCamera.setCameraDisplayOrientation;


/**
 * Created by zhousong on 2016/9/19.
 * 相机界面SurfaceView的回调类
 */
public final class RecSurfaceViewCallback implements android.view.SurfaceHolder.Callback, Camera.PreviewCallback {

    Context context;
    static final String TAG = "Camera";
    FrontCamera mFrontCamera = new FrontCamera();
    boolean previewing = mFrontCamera.getPreviewing();
    Camera mCamera;
    FaceRecognizeTask mFaceTask;

    public void setContext(Context context) {
        this.context = context;
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        if (previewing) {
            mCamera.stopPreview();
            Log.i(TAG, "停止预览");
        }

        try {
            mCamera.setPreviewDisplay(arg0);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            Log.i(TAG, "开始预览");
            //调用旋转屏幕时自适应
//            setCameraDisplayOrientation(MainActivity.this, FrontCamera.mCurrentCamIndex, mCamera);
        } catch (Exception e) {
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //初始化前置摄像头
        mFrontCamera.setCamera(mCamera);
        mCamera = mFrontCamera.initCamera();
        mCamera.setPreviewCallback(this);
        //适配竖排固定角度
        Log.e(TAG, "context: " + context.toString());
        Log.e(TAG, "mFrontCamera: " + mFrontCamera.toString());
        Log.e(TAG, "mCamera: " + mCamera.toString());
        setCameraDisplayOrientation((Activity) context, mFrontCamera.getCurrentCamIndex(), mCamera);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mFrontCamera.StopCamera(mCamera);
    }

    /**
     * 相机实时数据的回调
     *
     * @param data   相机获取的数据，格式是YUV
     * @param camera 相应相机的对象
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mFaceTask != null) {
            switch (mFaceTask.getStatus()) {
                case RUNNING:
                    return;
                case PENDING:
                    mFaceTask.cancel(false);
                    break;
            }

        }
        mFaceTask = new FaceRecognizeTask(data, camera);
        mFaceTask.execute((Void) null);
        //Log.i(TAG, "onPreviewFrame: 启动了Task");

    }

}