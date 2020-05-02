package com.charleyszc.faceDemo.mobilefacenet;
/**
 * Created by szc on 2019/05/13
 */
public class FaceEngine {
    //TODO: 人脸检测模型导入
    public static native int FaceModelInit(String faceDetectionModelPath);

    //TODO: 人脸检测
    public static native int[] FaceDetect(byte[] imageData, int imageWidth, int imageHeight, int imageChannel);

    //TODO: 检测最大脸
    public static native int[] MaxFaceDetect(byte[] imageData, int imageWidth, int imageHeight, int imageChannel);

    //TODO: 人脸检测模型反初始化
    public native boolean FaceModelUnInit();

    //TODO: 检测的最小人脸设置
    public static native boolean SetMinFaceSize(int minSize);

    //TODO: 线程设置 4线程效果最好
    public static native boolean SetThreadsNumber(int threadsNumber);

    //TODO: 循环测试次数
    public static native boolean SetTimeCount(int timeCount);

    //TODO: 人脸对齐 正脸
    public static native String FaceAlign(long frame, float[] landmarks);

    //TODO: 返回人脸三个位置之一的128D向量方法
    public static native float[] FaceArray(byte[] faceData);

    //TODO: 人脸比对
    public static native double FaceCompare(byte[] faceData1, int w1, int h1, byte[] faceDate2, int w2, int h2);

    //TODO: 添加人脸
    public static native boolean AddFace(byte[] faceData, int w, int h, String featurePath, String id);

    //TODO: 人脸识别
    public static native String Recognize(byte[] faceData, int w, int h, String featurePath, double threshold);

    //TODO:活体检测
//    public static native boolean LivenessDetecte(long frame);
    public static native boolean LivenessDetecte(byte[] livenessData, int w, int h);
    static {
        System.loadLibrary("charleyszcFace");
    }
}
