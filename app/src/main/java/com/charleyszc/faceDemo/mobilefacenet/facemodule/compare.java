package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


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
public class compare {

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
//        Log.e("MMMMMMM", frame.toString());
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
//        Bitmap BofFace = frame.copy(Bitmap.Config.ARGB_8888, true);

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
        float left, top, right, bottom;
        if (faceInfo.length > 1) {
            int faceNum = faceInfo[0];
            Log.i(TAG, "人脸数目：" + faceNum);

            for (int i = 0; i < faceNum; i++) {

                Canvas canvas = new Canvas(frame);
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);

//                float left, top, right, bottom;
                left = faceInfo[1 + 14 * i];
                top = faceInfo[2 + 14 * i];
                right = faceInfo[3 + 14 * i];
                bottom = faceInfo[4 + 14 * i];
                Log.e(TAG,"l:"+left+"=="+"t:"+top+"=="+"r:"+right+"=="+"b:"+bottom);
//                Point lefttop = new Point(left, top);
//                Point rightbottom = new Point(right, bottom);
                canvas.drawRect(left, top, right, bottom, paint);

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
                canvas.drawPoints(new float[]{
                        faceInfo[5+14*i],faceInfo[10+14*i],
                        faceInfo[6+14*i],faceInfo[11+14*i],
                        faceInfo[7+14*i],faceInfo[12+14*i],
                        faceInfo[8+14*i],faceInfo[13+14*i],
                        faceInfo[9+14*i],faceInfo[14+14*i]}, paint);

                //TODO: 正脸
                Log.e("alignedFace Addr: ", String.valueOf(alignedFace.getNativeObjAddr()));
                String pos = FaceEngine.FaceAlign(alignedFace.getNativeObjAddr(), landmarks);
                Log.e(TAG, "position: " + pos);
//                byte[] p = pos.getBytes();
//                Bitmap Bp = BitmapFactory.decodeByteArray(p, 0, p.length);
//                saveBitmap(Bp);
                Bitmap tmp = Bitmap.createBitmap(alignedFace.width(), alignedFace.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(alignedFace, tmp);
//                saveBitmap(tmp,"tmp010101");

//                byte[] alignedFaceData = getPixelsRGBA(tmp);
//                Log.e(TAG, "aligned Face Data size: " + alignedFaceData.length);
//
//                float[] faceData = FaceEngine.FaceArray(alignedFaceData);
//                Log.e(TAG, "Face Data Size: " + faceData);


//                Imgproc.rectangle(frameToMat, lefttop, rightbottom, new Scalar(255, 255, 0, 255), 2);
//                Imgproc.putText(frame, res, lefttop,Core.FONT_HERSHEY_SIMPLEX,2, new Scalar(255,255,0,255),2);
//                canvas.drawPoints(new float[]{faceInfo[5+14*i],faceInfo[10+14*i],
//                        faceInfo[6+14*i],faceInfo[11+14*i],
//                        faceInfo[7+14*i],faceInfo[12+14*i],
//                        faceInfo[8+14*i],faceInfo[13+14*i],
//                        faceInfo[9+14*i],faceInfo[14+14*i]}, paint);//画多个点

//                Bitmap AaF = Bitmap.createBitmap(alignedFace.width(), alignedFace.height(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(alignedFace, AaF);
//                saveBitmap(AaF, "01010101");
                return tmp;

            }
        } else {
            Log.e(TAG, "没有检测到人脸!!!");
//            tv_name.setText("Look at the Camera pls");
        }

        return null;
    }

    //TODO: 保存人脸图像
    public static void saveBitmap(Bitmap bitmap, String id){
        Log.e("saveBitmap","人脸图保存");

        File sdDir = Environment.getExternalStorageDirectory();//get directory
        File faceImg = new File(sdDir.toString()+"/facem/img/");
        //TODO: 检查是否有人脸图文件夹，没有则创建
        if (!faceImg.exists()){
            faceImg.mkdir();
        }

        //此处图片名是暂时的
        File faceName = new File(sdDir.toString()+"/facem/img/" + id +".png");

        try {
            FileOutputStream outPutImg = new FileOutputStream(faceName);
            bitmap.compress(Bitmap.CompressFormat.PNG,100, outPutImg);
            out.flush();
            out.close();
            Log.e("人脸图", "已保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //TODO: 添加人脸（保存人脸特征文件）
    public static void addFace(byte[] face, int w, int h, String id){
        //TODO: get directory获取文件夹
        File sdDir = Environment.getExternalStorageDirectory();
        String faceFeaturePath = sdDir.toString()+"/facem/facedata/";
        File faceFeatureFilePath = new File(faceFeaturePath);

        //TODO: 检查人脸特征文件文件夹，没有则创建；
        if (!faceFeatureFilePath.exists()){
            faceFeatureFilePath.mkdir();
        }

        //此处的ID是暂时的
        FaceEngine.AddFace(face, w, h, faceFeaturePath, id);
    }
}
