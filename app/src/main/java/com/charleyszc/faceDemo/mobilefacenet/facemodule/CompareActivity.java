package com.charleyszc.faceDemo.mobilefacenet.facemodule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.charleyszc.faceDemo.mobilefacenet.FaceEngine;
import com.charleyszc.faceDemo.mobilefacenet.R;

import java.io.File;
import java.io.FileNotFoundException;

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.compare.getPixelsRGBA;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.compare.startDetectFace;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.compare.verifyStoragePermissions;


/**
 * Created by szc on 2019/05/13
 */
public class CompareActivity extends AppCompatActivity {

    private static final int SELECT_IMAGE1 = 1,
            SELECT_IMAGE2 = 2;

    private ImageView imageView1, imageView2;

    public static Bitmap yourSelectedImage1 = null;
    public Bitmap yourSelectedImage2 = null;

    private Bitmap faceImage1 = null,
            faceImage2 = null;

    private Bitmap detectedImg1 = null,
            detectedImg2 = null;

    String sdPath;

    TextView faceInfo1, faceInfo2, cmpResult;

    private boolean maxFaceSetting = false;

    String id1, id2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        //TODO: 确认权限
        verifyStoragePermissions(this);

        //TODO: model init
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        sdPath = sdDir.toString() + "/facem/";

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
//                saveBitmap(detectedImg1, id1);

            }
        });

        //TODO: RIGHT IMAGE
        imageView2 = (ImageView) findViewById(R.id.imageView2);
        faceInfo2=(TextView)findViewById(R.id.faceInfo2);
        Button buttonImage2 = (Button) findViewById(R.id.select2);

        //TODO: 右侧图片读取
        buttonImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE2);
            }
        });

        Button buttonDetect2 = (Button) findViewById(R.id.detect2);
        buttonDetect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (yourSelectedImage2 == null)
                    return;
                //detect
                faceImage2=null;

//                saveBitmap(yourSelectedImage2);

                detectedImg2 = startDetectFace(yourSelectedImage2,true);
                id2 = String.valueOf(System.currentTimeMillis());
//                saveBitmap(detectedImg2, id2);

            }
        });

        //TODO: cmp
        cmpResult=(TextView)findViewById(R.id.textView1);
        Button cmpImage = (Button) findViewById(R.id.facecmp);
        cmpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (detectedImg1 == null||detectedImg2 == null){
                    cmpResult.setText("no enough face,return");
                    return;
                }
                byte[] faceDate1 = getPixelsRGBA(detectedImg1);
                byte[] faceDate2 = getPixelsRGBA(detectedImg2);
                long timeRecognizeFace = System.currentTimeMillis();

//                addFace(faceDate1, detectedImg1.getWidth(), detectedImg1.getHeight(), id1);
//                addFace(faceDate2, detectedImg2.getWidth(), detectedImg2.getHeight(), id2);

                Log.e("ImageSize", "face1: " + detectedImg1.getWidth() + "," + detectedImg1.getHeight());
                Log.e("ImageSize", "face2: " + detectedImg2 .getWidth() + "," + detectedImg2.getHeight());

                double similar = FaceEngine.FaceCompare(faceDate1, detectedImg1.getWidth(), detectedImg1.getHeight(),
                        faceDate2, detectedImg2.getWidth(), detectedImg2.getHeight());

                timeRecognizeFace = System.currentTimeMillis() - timeRecognizeFace;
                cmpResult.setText("cosin:"+similar+"\n"+"cmp time:"+timeRecognizeFace);

                Log.e("Similar", String.valueOf(similar));
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
                else if (requestCode == SELECT_IMAGE2) {
                    Bitmap bitmap =decodeUri(selectedImage);
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yourSelectedImage2 = rgba;
                    imageView2.setImageBitmap(yourSelectedImage2);
                }
            } catch (FileNotFoundException e) {
                Log.e("CompareActivity", "FileNotFoundException");
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
