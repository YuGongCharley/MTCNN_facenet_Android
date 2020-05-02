package com.charleyszc.faceDemo.mobilefacenet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.charleyszc.faceDemo.mobilefacenet.facemodule.AddPersonActivity;
import com.charleyszc.faceDemo.mobilefacenet.facemodule.CompareActivity;
import com.charleyszc.faceDemo.mobilefacenet.facemodule.TestLivenessActivity;
import com.charleyszc.faceDemo.mobilefacenet.facemodule.TestRecognizeActivity;
import com.charleyszc.faceDemo.mobilefacenet.facemodule.VideoAddFaceActivity;
import com.charleyszc.faceDemo.mobilefacenet.facemodule.VideoRecognizeActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import static android.content.ContentValues.TAG;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.recognize.verifyStoragePermissions;

/**
 * Created by szc on 2019/05/13
 */
public class MainActivity extends AppCompatActivity {

    File sdDir = Environment.getExternalStorageDirectory();//get directory
    String sdPath = sdDir.toString() + "/facem/";

    @Override
    protected void onResume() {
        super.onResume();
        FaceEngine.FaceModelInit(sdPath);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);

        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
//                    mOpenCvCameraView.enableView();
//                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: 确认权限
        verifyStoragePermissions(this);

        try {
            copyBigDataToSD("det1.bin");
            copyBigDataToSD("det2.bin");
            copyBigDataToSD("det3.bin");
            copyBigDataToSD("det1.param");
            copyBigDataToSD("det2.param");
            copyBigDataToSD("det3.param");
            copyBigDataToSD("recognition1.bin");
            copyBigDataToSD("recognition1.param");
//            copyBigDataToSD("NL_nir_an.bin");
//            copyBigDataToSD("NL_nir_an.param");
//            copyBigDataToSD("NL_nir_gn.bin");
//            copyBigDataToSD("NL_nir_gn.param");
            copyBigDataToSD("nir_nfd_gn7.bin");
            copyBigDataToSD("nir_nfd_gn7.param");

//            copyBigDataToSD("mobilefacenet.bin");
//            copyBigDataToSD("mobilefacenet.param");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO; model init
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        String sdPath = sdDir.toString() + "/facem/";
        FaceEngine.FaceModelInit(sdPath);
    }

    //TODO: 将模型考入SD卡
    public void copyBigDataToSD(String strOutFileName) throws IOException {
        Log.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        File file = new File(sdDir.toString()+"/facem/");
        if (!file.exists()) {
            file.mkdir();
        }

        String tmpFile = sdDir.toString()+"/facem/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Log.i(TAG, "file exists " + strOutFileName);
            return;
        }

        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString()+"/facem/"+ strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
        Log.i(TAG, "end copy file " + strOutFileName);

    }

    public void jumpToCompareView(View view) {
        startActivity(new Intent(this, CompareActivity.class));
    }

    public void jumpToAddPersonView(View view) {
        startActivity(new Intent(this, AddPersonActivity.class));
    }

    public void jumpToVideoAddPersonView(View view){
        startActivity(new Intent(this, VideoAddFaceActivity.class));
    }

    public void jumpToTestRecognizeView(View view){
        startActivity(new Intent(this, TestRecognizeActivity.class));
    }

    public void jumpToVideoRecognizeView(View view) {
        startActivity(new Intent(this, VideoRecognizeActivity.class));
    }

    public void jumpToTestLivenessView(View view) {
        startActivity(new Intent(this, TestLivenessActivity.class));
    }

}
