package com.charleyszc.faceDemo.mobilefacenet.frontcamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.charleyszc.faceDemo.mobilefacenet.util.PhotoBitmapUtils;

import java.io.ByteArrayOutputStream;

import static com.charleyszc.faceDemo.mobilefacenet.facemodule.addPerson.addFace;
import static com.charleyszc.faceDemo.mobilefacenet.facemodule.recognize.startDetectFace;


/**
 * Created by szc on 2019/06/20
 * 单独的任务类。继承AsyncTask，来处理从相机实时获取的耗时操作
 */
public class FaceAddTask extends AsyncTask {
    private byte[] mData;
    Camera mCamera;
    private static final String TAG = "CameraAddTag";

    int i = 0;
    private Context context;
    boolean addResult;

    //构造函数
    public FaceAddTask(byte[] data, Camera camera)
    {
        this.mData = data;
        this.mCamera = camera;

    }
    @Override
    protected Object doInBackground(Object[] params) {
        Camera.Parameters parameters = mCamera.getParameters();
        int imageFormat = parameters.getPreviewFormat();
        int w = parameters.getPreviewSize().width;
        int h = parameters.getPreviewSize().height;

        Rect rect = new Rect(0, 0, w, h);
        YuvImage yuvImg = new YuvImage(mData, imageFormat, w, h, null);
        try {
            ByteArrayOutputStream byteImg = new ByteArrayOutputStream();
            yuvImg.compressToJpeg(rect, 100, byteImg);
            Bitmap rawbitmap = BitmapFactory.decodeByteArray(byteImg.toByteArray(), 0, byteImg.size());
            //TODO: 旋转
            //开发板
//            rawbitmap = PhotoBitmapUtils.rotaingImageView(90,rawbitmap);
            //vivo r71a
            rawbitmap = PhotoBitmapUtils.rotaingImageView(270,rawbitmap);
            Log.e(TAG, "onPreviewFrame: rawbitmap:" + rawbitmap.toString());
            i++;
            //face：检测出的人脸
            Bitmap face = startDetectFace(rawbitmap, true);
            if (face!=null) {
//                saveBitmap(face, System.currentTimeMillis() + "-" + i);
                addResult = addFace(face, face.getWidth(), face.getHeight(), String.valueOf(System.currentTimeMillis()));
                if (addResult==true){
                    Toast.makeText(context,"添加成功",Toast.LENGTH_LONG).show();
//                    startActivity(new Intent(this, MainActivity.class));
                }
            }
            //若要存储可以用下列代码，格式为jpg
//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/fp.jpg"));
//            yuvImg.compressToJpeg(rect, 100, bos);
//            bos.flush();
//            bos.close();
            mCamera.startPreview();
        }
        catch (Exception e)
        {
            Log.e(TAG, "onAddPreviewFrame: 获取相机实时数据失败" + e.getLocalizedMessage());
        }
        return null;
    }



}
