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

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.compare.getPixelsRGBA;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.recognize.verifyStoragePermissions;

/**
 * Created by szc on 2019/06/28
 */
public class TestLivenessActivity extends AppCompatActivity {

    private static final int SELECT_RECIMAGE1 = 1;

    private ImageView imageLiveView1, imageLiveView2;

    private Bitmap yourSelectedLiveImage1 = null;

    private Bitmap faceLiveImage1 = null,
            faceLiveImage2 = null;

    private Bitmap livenessImg1 = null;
    String sdPath;

    TextView faceLiveInfo1, faceLiveInfo2, liveResult;

    private boolean maxFaceSetting = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveness);
        //TODO: 确认权限
        verifyStoragePermissions(this);

        //TODO: model init
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        sdPath = sdDir.toString() + "/facem/";

        //TODO: LEFT IMAGE
        imageLiveView1 = (ImageView) findViewById(R.id.imageLiveView1);
        faceLiveInfo1=(TextView)findViewById(R.id.faceLiveInfo1);
        Button buttonRecImage1 = (Button) findViewById(R.id.selectLive1);

        //TODO: 左侧照片读取
        buttonRecImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_RECIMAGE1);

//                if (yourSelectedRecImage1 == null)
//                    return;
//                faceRecImage1=null;
//
////                saveBitmap(yourSelectedRecImage1);
//
//                recognizedImg1 = startDetectFace(yourSelectedRecImage1,true);
//                saveBitmap(recognizedImg1);

            }
        });

        //TODO: liveness detecte
        liveResult=(TextView)findViewById(R.id.faceLiveInfo2);
        Button liveImage = (Button) findViewById(R.id.btn_faceLiveDet);
        liveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (livenessImg1 == null){
                    liveResult.setText("no face,return");
                    return;
                }
                byte[] faceDate1 = getPixelsRGBA(livenessImg1);
//                long timeRecognizeFace = System.currentTimeMillis();

                boolean live = liveness.startLivenessDetecte(livenessImg1);
                Log.e("ImageSize", "face1: " + livenessImg1.getWidth() + "," + livenessImg1.getHeight());

//                String id = recognize.recognizeFace(livenessImg1, livenessImg1.getWidth(), livenessImg1.getHeight(), threshold);

//                timeRecognizeFace = System.currentTimeMillis() - timeRecognizeFace;
                if (live==true){

                    liveResult.setText("Liveness detect: true");
                }else {
                    liveResult.setText("Liveness detect: false");
                }

//                Log.e("Person ID", String.valueOf(id));
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
                    yourSelectedLiveImage1 = rgba;
                    imageLiveView1.setImageBitmap(yourSelectedLiveImage1);
                    if (yourSelectedLiveImage1 == null)
                        return;
                    faceLiveImage1=null;

//                saveBitmap(yourSelectedRecImage1);
                    livenessImg1 = yourSelectedLiveImage1;
//                    livenessImg1 = startDetectFace(yourSelectedLiveImage1,true);
//                    recognizedImg1 = yourSelectedRecImage1;
//                    saveBitmap(recognizedImg1);
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
