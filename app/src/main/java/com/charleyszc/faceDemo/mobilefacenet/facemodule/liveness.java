package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import com.charleyszc.faceDemo.mobilefacenet.FaceEngine;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import static java.lang.System.out;

/**
 * Created by szc on 2019/06/28
 */
public class liveness {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    //TODO: 存储权限认证
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: 保存人脸图像
    public static void saveBitmap(Bitmap bitmap){
        Log.e("saveBitmap","人脸图保存");

        File sdDir = Environment.getExternalStorageDirectory();//get directory
        File faceImg = new File(sdDir.toString()+"/facem/img/");
        //TODO: 检查是否有人脸图文件夹，没有则创建
        if (!faceImg.exists()){
            faceImg.mkdir();
        }

        //此处图片名是暂时的
        File faceName = new File(sdDir.toString()+"/facem/img/" + String.valueOf(System.currentTimeMillis())+".jpg");

        try {
            FileOutputStream outPutImg = new FileOutputStream(faceName);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutImg);
            out.flush();
            out.close();
            Log.e("人脸图", "已保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //TODO: get pixels获取Bitmap像素数据
    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    public static boolean startLivenessDetecte(Bitmap frame){
        byte[] facedata = getPixelsRGBA(frame);
//        Mat facedata = new Mat();
//        Utils.bitmapToMat(frame,facedata);
//        FaceEngine.LivenessDetecte(facedata.getNativeObjAddr());
        boolean livedetect = FaceEngine.LivenessDetecte(facedata, frame.getWidth(), frame.getHeight());
        if (livedetect==true){
            return true;
        }else {
            return false;
        }

    }
}
