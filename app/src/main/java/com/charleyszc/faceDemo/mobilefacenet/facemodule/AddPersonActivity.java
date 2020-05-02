package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.charleyszc.faceDemo.mobilefacenet.R;
import com.example.lyf.yflibrary.Permission;
import com.example.lyf.yflibrary.PermissionResult;


import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileNotFoundException;

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.addFace;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.delFile;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.getFileName;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.saveBitmap;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.startDetectFace;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.verifyStoragePermissions;
import static org.opencv.imgcodecs.Imgcodecs.imread;

/**
 * Created by szc on 2019/05/14
 */
public class AddPersonActivity extends AppCompatActivity {
    private static final int SELECT_IMAGE1 = 1;

    private ImageView imageView1;

    private Bitmap yourSelectedImage1 = null;

    private Bitmap faceImage1 = null;

    private Bitmap detectedImg1 = null,
            detectedImg2 = null;

    String sdPath;
    String mFacePath;

    TextView faceInfo1, cmpResult;

    private boolean maxFaceSetting = false;

    String id1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addface);
        //TODO: 确认权限
        verifyStoragePermissions(this);

        //TODO: model init
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        sdPath = sdDir.toString() + "/facem/";
        mFacePath = sdPath + "mface/";

        //TODO: LEFT IMAGE
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        faceInfo1=(TextView)findViewById(R.id.faceInfo1);
        Button buttonImage1 = (Button) findViewById(R.id.select1);

        //TODO: 左侧照片读取
        buttonImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE1);
            }
        });

        Button buttonDetect1 = (Button) findViewById(R.id.detect1);
        buttonDetect1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage1 == null)
                    return;
                faceImage1=null;

//                saveBitmap(yourSelectedImage1);

                detectedImg1 = startDetectFace(yourSelectedImage1,true);
                id1 = String.valueOf(System.currentTimeMillis());
                saveBitmap(detectedImg1, id1);

            }
        });


        //TODO: add
        cmpResult=(TextView)findViewById(R.id.textView1);
        Button addFace = (Button) findViewById(R.id.faceadd);
        addFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (detectedImg1 == null){
                    cmpResult.setText("no enough face,return");
                    return;
                }
//                byte[] faceData1 = getPixelsRGBA(detectedImg1);
//                long timeRecognizeFace = System.currentTimeMillis();

                addFace(detectedImg1, detectedImg1.getWidth(), detectedImg1.getHeight(), id1);

                Log.e("ImageSize", "face1: " + detectedImg1.getWidth() + "," + detectedImg1.getHeight());

//                timeRecognizeFace = System.currentTimeMillis() - timeRecognizeFace;

            }
        });

        //TODO: add M
        Button addMFace = findViewById(R.id.face_m_add);
        addMFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] imgNames;
                String imgName;

                File mFaceFile = new File(mFacePath);
                if (mFaceFile.exists()){
                    imgNames = getFileName(mFacePath);
                }else {
                    mFaceFile.mkdir();
                    cmpResult.setText("no mface file, return");
                    return;
                }
                for (int i=0; i<imgNames.length; i++){
                    try{
                        String mId = String.valueOf(System.currentTimeMillis());
                        imgName = imgNames[i];
                        File getImage = new File(mFacePath+imgName);
                        Bitmap bitmap = decodeUri(Uri.fromFile(getImage));
                        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        if (rgba==null){
                            continue;
                        }else {
                            Bitmap detectFace = startDetectFace(rgba,true);
                            if(detectFace!=null){
                                saveBitmap(detectFace,mId);
//                                byte[] faceData1 = getPixelsRGBA(detectFace);
                                addFace(detectFace, detectFace.getWidth(), detectFace.getHeight(), mId);
                                delFile(mFacePath+imgName);
                            }else {
                                continue;
                            }

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }

                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                if (requestCode == SELECT_IMAGE1) {
                    Bitmap bitmap = decodeUri(selectedImage);
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yourSelectedImage1 = rgba;
                    imageView1.setImageBitmap(yourSelectedImage1);
                }
            } catch (FileNotFoundException e) {
                Log.e("AddPersonActivity", "FileNotFoundException");
                return;
            }
        }
    }

    public Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

}
