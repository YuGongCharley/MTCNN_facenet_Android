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


import com.charleyszc.faceDemo.mobilefacenet.R;

import java.io.File;
import java.io.FileNotFoundException;

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.compare.startDetectFace;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.liveness.saveBitmap;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.recognize.verifyStoragePermissions;

/**
 * Created by szc on 2019/05/14
 */
public class TestRecognizeActivity extends AppCompatActivity {

    private static final int SELECT_RECIMAGE1 = 1;

    private ImageView imageRecView1, imageRecView2;

    private Bitmap yourSelectedRecImage1 = null;

    private Bitmap faceRecImage1 = null,
            faceRecImage2 = null;

    private Bitmap recognizedImg1 ;
    String sdPath;

    TextView faceRecInfo1, faceRecInfo2, recResult;

    private boolean maxFaceSetting = false;

    double threshold = 0.55;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testrecognize);
        //TODO: 确认权限
        verifyStoragePermissions(this);

        //TODO: model init
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        sdPath = sdDir.toString() + "/facem/";

        //TODO: LEFT IMAGE
        imageRecView1 = (ImageView) findViewById(R.id.imageRecView1);
        faceRecInfo1=(TextView)findViewById(R.id.faceRecInfo1);
        Button buttonRecImage1 = (Button) findViewById(R.id.selectRec1);

        //TODO: 左侧照片读取
        buttonRecImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_RECIMAGE1);
            }
        });

        //TODO: rec
        recResult=(TextView)findViewById(R.id.faceRecInfo2);
        Button recImage = (Button) findViewById(R.id.btn_faceRec);
        recImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (recognizedImg1 == null){
                    recResult.setText("no face,return");
                    return;
                }
                long timeRecognizeFace = System.currentTimeMillis();


                Log.e("ImageSize", "face1: " + recognizedImg1.getWidth() + "," + recognizedImg1.getHeight());

                String id = recognize.recognizeFace(recognizedImg1, recognizedImg1.getWidth(), recognizedImg1.getHeight(), threshold);

                timeRecognizeFace = System.currentTimeMillis() - timeRecognizeFace;
                recResult.setText("Person ID:"+id+"\n"+"Rec time:"+timeRecognizeFace);

                Log.e("Person ID", String.valueOf(id));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                if (requestCode == SELECT_RECIMAGE1) {
                    Bitmap bitmap = decodeUri(selectedImage);
                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yourSelectedRecImage1 = rgba;
                    imageRecView1.setImageBitmap(yourSelectedRecImage1);
                    if (yourSelectedRecImage1 == null)
                        return;
//                    faceRecImage1=null;

//                saveBitmap(yourSelectedRecImage1);

                    recognizedImg1 = startDetectFace(yourSelectedRecImage1,true);
//                    recognizedImg1 = yourSelectedRecImage1;
                    saveBitmap(recognizedImg1);
                }
            } catch (FileNotFoundException e) {
                Log.e("TestRecognizeActivity", "FileNotFoundException");
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
