package com.charleyszc.faceDemo.mobilefacenet.uvc;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PreviewImage {
	public static final int DATA_TYPE_YUV = 1;
	public static final int DATA_TYPE_BGR = 2;
	public static final int DATA_TYPE_JPG = 3;
	public static final int DATA_TYPE_PNG = 4;

	public int mWidth = 0;
	public int mHeight = 0;
	public byte[] mData = null;
	public int mDataType = DATA_TYPE_YUV;
	public int mOrient = 0;

	//识别人脸匡
	public float mLeft = 0;
	public float mTop = 0;
	public float mRight = 0;
	public float mBottom = 0;
	
    public static PreviewImage colorData = new PreviewImage();
    public static PreviewImage redData = new PreviewImage();
    
    private static ReentrantReadWriteLock colorLock = new ReentrantReadWriteLock();
    private static ReentrantReadWriteLock redLock = new ReentrantReadWriteLock();
    
    public static void setColorData(byte[] data, int width, int height, int data_type, int orient) {
    	colorLock.writeLock().lock();
    	colorData.mData = data;
    	colorData.mWidth = width;
    	colorData.mHeight = height;
    	colorData.mDataType = data_type;
    	colorData.mOrient = orient;
    	colorLock.writeLock().unlock();
    }

    public static void setRedData(byte[] data, int width, int height, int data_type, int orient) {
    	redLock.writeLock().lock();
    	redData.mData = data;
    	redData.mWidth = width;
    	redData.mHeight = height;
    	redData.mDataType = data_type;
    	redData.mOrient = orient;
    	redLock.writeLock().unlock();
    }

    public static PreviewImage getColorData() {
    	PreviewImage ret = new PreviewImage();
    	colorLock.readLock().lock();
    	ret.mWidth = colorData.mWidth;
    	ret.mHeight = colorData.mHeight;
    	ret.mDataType = colorData.mDataType;
    	ret.mOrient = colorData.mOrient;
    	if (colorData.mData != null) {
    		ret.mData = new byte[colorData.mData.length];
    		System.arraycopy(colorData.mData, 0, ret.mData, 0, colorData.mData.length);
    	}
    	colorLock.readLock().unlock();
    	return ret;
    }
    

    public static PreviewImage getRedData() {
    	PreviewImage ret = new PreviewImage();
    	redLock.readLock().lock();
    	ret.mWidth = redData.mWidth;
    	ret.mHeight = redData.mHeight;
    	ret.mDataType = redData.mDataType;
    	ret.mOrient = redData.mOrient;
    	if (redData.mData != null) {
    		ret.mData = new byte[redData.mData.length];
    		System.arraycopy(redData.mData, 0, ret.mData, 0, redData.mData.length);
    	} else {
    		redData.mData = null;
    		ret.mWidth = 0;
    		ret.mHeight = 0;
    	}
    	redLock.readLock().unlock();
    	return ret;
    }

    //add by ljj
	public static void setDetectResultColor(float left,float top,float right,float bottom ){
		colorLock.writeLock().lock();
		colorData.mLeft = left;
		colorData.mTop = top;
		colorData.mRight = right;
		colorData.mBottom = bottom;
		colorLock.writeLock().unlock();
	}

	public static PreviewImage getDetectResultColor(){
		PreviewImage ret = new PreviewImage();
		colorLock.readLock().lock();
		ret.mLeft = colorData.mLeft;
		ret.mBottom = colorData.mBottom;
		ret.mRight = colorData.mRight;
		ret.mTop = colorData.mTop;
		colorLock.readLock().unlock();
		return ret;
	}
}
