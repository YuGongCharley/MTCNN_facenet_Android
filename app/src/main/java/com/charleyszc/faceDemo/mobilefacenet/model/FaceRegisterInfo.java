package com.charleyszc.faceDemo.mobilefacenet.model;

//TODO: 人脸信息注册
public class FaceRegisterInfo {
//    private byte[] featureData;
    private String id;

    public FaceRegisterInfo(String id) {
//        this.featureData = faceFeature;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String name) {
        this.id = name;
    }

//    public byte[] getFeatureData() {
//        return featureData;
//    }
//
//    public void setFeatureData(byte[] featureData) {
//        this.featureData = featureData;
//    }
}
