package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceHolder;

import com.charleyszc.faceDemo.mobilefacenet.FaceEngine;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import static android.content.ContentValues.TAG;
import static java.lang.System.out;

/**
 * Created by szc on 2019/05/13
 */
public class recognize {
    // 控制模型参数
    private static int minFaceSize = 40;
    private static int testTimeCount = 10;
    private static int threadsNumber = 4;
    // 是否打开仅最大脸检测
    private static boolean maxFaceSetting = false;

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



    //TODO: get pixels获取Bitmap像素数据
    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    //TODO: startDetectedFace 开始检测人脸
    public static Bitmap startDetectFace(Bitmap frame, boolean detectMode) {
//        saveBitmap(frame);
        Log.e("MMMMMMM", frame.toString());
        maxFaceSetting = detectMode;
        FaceEngine.SetMinFaceSize(minFaceSize);
        FaceEngine.SetTimeCount(testTimeCount);
        FaceEngine.SetThreadsNumber(threadsNumber);

        Mat frameToMat = new Mat();
        Utils.bitmapToMat(frame, frameToMat);
        Bitmap pic = Bitmap.createBitmap(frameToMat.width(), frameToMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frameToMat, pic);
        Mat alignedFace = frameToMat.clone();

        int width = pic.getWidth();
        int height = pic.getHeight();
        byte[] imageData = getPixelsRGBA(pic);

        long timeDetectFace = System.currentTimeMillis();
        int faceInfo[] = null;
//        if (maxFaceSetting) {
//            faceInfo = MTCNN.FaceDetect(imageData, width, height, 4);
//            Log.i(TAG, "检测所有人脸");
//            Log.e("FaceDetect", String.valueOf(faceInfo.length));
//        } else {
        faceInfo = FaceEngine.MaxFaceDetect(imageData, width, height, 4);
        Log.e("MaxFaceDetect", String.valueOf(faceInfo));
        Log.i(TAG, "检测最大人脸");
//        }
        timeDetectFace = System.currentTimeMillis() - timeDetectFace;
        Log.i(TAG, "人脸平均检测时间：" + timeDetectFace / testTimeCount);
        int left, top, right, bottom;
        if (faceInfo.length > 1) {
            int faceNum = faceInfo[0];
            Log.i(TAG, "人脸数目：" + faceNum);

            for (int i = 0; i < faceNum; i++) {

//                Canvas canvas = new Canvas();
//                canvas = surfaceHolder.lockCanvas();
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画
//                Paint paint = new Paint();
//                paint.setColor(Color.GREEN);
//                paint.setAntiAlias(true);//去锯齿
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(5f);

                left = faceInfo[1 + 14 * i];
                top = faceInfo[2 + 14 * i];
                right = faceInfo[3 + 14 * i];
                bottom = faceInfo[4 + 14 * i];
//                CameraSurfaceHolder.drawRectangle(left,top,right,bottom);
                Log.e(TAG,"l:"+left+"=="+"t:"+top+"=="+"r:"+right+"=="+"b:"+bottom);

                Point lefttop = new Point(left, top);
                Point rightbottom = new Point(right, bottom);
//                canvas.drawRect(left, top, right, bottom, paint);

                // 传入人脸五个特征坐标
                float[] landmarks = new float[10];
                landmarks[0] = faceInfo[5 + 14 * i];
                landmarks[1] = faceInfo[6 + 14 * i];
                landmarks[2] = faceInfo[7 + 14 * i];
                landmarks[3] = faceInfo[8 + 14 * i];
                landmarks[4] = faceInfo[9 + 14 * i];
                landmarks[5] = faceInfo[10 + 14 * i];
                landmarks[6] = faceInfo[11 + 14 * i];
                landmarks[7] = faceInfo[12 + 14 * i];
                landmarks[8] = faceInfo[13 + 14 * i];
                landmarks[9] = faceInfo[14 + 14 * i];

                //TODO: 画特征点
//                canvas.drawPoints(new float[]{
//                        faceInfo[5+14*i],faceInfo[10+14*i],
//                        faceInfo[6+14*i],faceInfo[11+14*i],
//                        faceInfo[7+14*i],faceInfo[12+14*i],
//                        faceInfo[8+14*i],faceInfo[13+14*i],
//                        faceInfo[9+14*i],faceInfo[14+14*i]}, paint);

                //TODO: 正脸
                String pos = FaceEngine.FaceAlign(alignedFace.getNativeObjAddr(), landmarks);
                Log.e(TAG, "position: " + pos);

                Bitmap tmp = Bitmap.createBitmap(alignedFace.width(), alignedFace.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(alignedFace, tmp);

                Imgproc.rectangle(frameToMat, lefttop, rightbottom, new Scalar(255, 255, 0, 255), 2);
//                Imgproc.putText(frame, res, lefttop,Core.FONT_HERSHEY_SIMPLEX,2, new Scalar(255,255,0,255),2);
//                canvas.drawPoints(new float[]{faceInfo[5+14*i],faceInfo[10+14*i],
//                        faceInfo[6+14*i],faceInfo[11+14*i],
//                        faceInfo[7+14*i],faceInfo[12+14*i],
//                        faceInfo[8+14*i],faceInfo[13+14*i],
//                        faceInfo[9+14*i],faceInfo[14+14*i]}, paint);//画多个点

                Bitmap AaF = Bitmap.createBitmap(alignedFace.width(), alignedFace.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(alignedFace, AaF);
//                frame = AaF;
//                saveBitmap(AaF);
//                frame.recycle();
                return AaF;

            }
        } else {
            Log.e(TAG, "没有检测到人脸!!!");
//            tv_name.setText("Look at the Camera pls");
        }

        return null;
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

    //TODO: recognize人脸比对
    public static String recognizeFace(Bitmap face, int w, int h ,double threshold){
        //TODO: get directory获取文件夹
        byte[] facedata = getPixelsRGBA(face);
        File sdDir = Environment.getExternalStorageDirectory();
        String faceFeaturePath = sdDir.toString()+"/facem/facedata/";
        Log.e("开始识别", facedata +" | "+ w +" | "+ h +" | "+ faceFeaturePath);
        String id = FaceEngine.Recognize(facedata, w, h, faceFeaturePath, threshold);
//        String id = "1212123123123123123";
        Log.e("User Id", id);

        return id;
    }
}
