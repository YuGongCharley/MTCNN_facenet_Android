package com.charleyszc.faceDemo.mobilefacenet.frontcamera;

import android.content.Context;
import android.graphics.Rect;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by zhousong on 2016/9/18.
 * 相机界面SurfaceView的Holder类
 */
public class CameraSurfaceHolder {
    Context context;
    static SurfaceHolder surfaceHolder;
    SurfaceView surfaceView;
    RecSurfaceViewCallback recCallback = new RecSurfaceViewCallback();

//    private static boolean mFaceDetectSupported  = false;  // 保存机器对人脸检测的支持情况
//    private static Integer mFaceDetectMode;                // 保存人脸检测模式
//    private static Rect cRect;                             // 保存成像区域
//    private static Size cPixelSize;

    /**
    * 设置相机界面SurfaceView的Holder
     * @param context 从相机所在的Activity传入的context
     * @param surfaceView Holder所绑定的响应的SurfaceView
    * */
    //TODO: 识别
    public void setRecCameraSurfaceHolder(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(recCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        recCallback.setContext(context);
    }
    //TODO: 添加
    public void setAddCameraSurfaceHolder(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(recCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        recCallback.setContext(context);
    }




}


