//
// Created by SongZichen on 2019-05-13.
//
//#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include <iostream>
#include <sstream>
#include <cstring>

// ncnn
#include "net.h"
#include <opencv2/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <dirent.h>
#include "detecte.h"
#include "FaceAlign.h"
#include "FaceNet.h"
#include "Liveness.h"

using namespace std;

#define TAG "CharleyfaceSo"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
static MTCNN *mtcnn;
static FaceAlign *aligner;
static FaceNet *facenet;
static Liveness *liveness;
//static Recognize *recognize;

//sdk是否初始化成功
bool detection_sdk_init_ok = false;

extern "C" {
//TODO: Sort 比较大小 返回最大数的下标
int Sort(vector<double> list, double threshold){
    int i,j=0;
    int id=0;

//    LOGE("SORT: %d",list.size());
    for (i=1; i<list.size(); i++){
        LOGE("s-for: %d", i);

        if ((list[i]>list[j]) && (list[i]>threshold)){
            LOGE("s-f-if: %d", i);
            j = i;
            id = i;
           LOGE("if-id: %d", id);
        }
    }

    if ((i==list.size()-1) || (id==0 && j==0 && list[id]<threshold) ){
        id = list.size()+1;
    }else if(list[id]<0.4){
        id = list.size()+1;
    }
    list.clear();

//    LOGE("BS: %d", id);
    return id;
}

//TODO: jstring转string
std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

//TODO: showAllId 显示全部id
vector<string> showAllId(string dir_name) {

    LOGE("Show All Ids!!!!!");
    // check the parameter
    vector<string> files ;
    int fileNum = 0;

    if (dir_name.empty()) {
        LOGE("dir_name is null !");
//        return;
    }
    DIR *dir = opendir(dir_name.c_str());
    // check is dir ?
    if (NULL == dir) {
        LOGE("Can not open dir. Check path or permission!");
//        return;
    }
    struct dirent *file;
    // read all the files in dir
    while ((file = readdir(dir)) != NULL) {

        // skip "." and ".."
        if (strcmp(file->d_name, ".") == 0 || strcmp(file->d_name, "..") == 0) {
//            LOGE("ignore . and ..");
            continue;
        }
        if (file->d_type == DT_DIR) {
            string filePath = dir_name + "/" + file->d_name;
            showAllId(filePath); // 递归执行
        } else {
            fileNum++;
            // 如果需要把路径保存到集合中去，就在这里执行 add 的操作
            files.push_back(file->d_name);
            LOGE("fileId: %d,filePath: %s", fileNum-1, file->d_name);
        }
    }
    closedir(dir);
    return files;
}

//TODO: FaceDetectionModelInit 人脸识别模型初始化方法
JNIEXPORT jint JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_FaceModelInit(JNIEnv *env, jobject instance,
                                                jstring faceDetectionModelPath_) {
     LOGE("JNI开始人脸检测模型初始化");
     int initCorrect = 0;
     int alreadyInit = 1;
     int initFail = 2;

    //如果已初始化则直接返回
    if (detection_sdk_init_ok) {
        LOGE("人脸检测模型已经导入");
        return alreadyInit;
    }
    jboolean tRet = false;
    if (NULL == faceDetectionModelPath_) {
        LOGE("导入的人脸检测的目录为空");
        return initFail;
    }

    //获取MTCNN模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *faceDetectionModelPath = env->GetStringUTFChars(faceDetectionModelPath_, 0);
    if (NULL == faceDetectionModelPath) {
        return initFail;
    }

    string tFaceModelDir = faceDetectionModelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length() - 1, 1);
    //LOGD("init, tFaceModelDir last =%s", tLastChar.c_str());
    //目录补齐/
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }
    LOGE("init, tFaceModelDir=%s", tFaceModelDir.c_str());

    //没判断是否正确导入，懒得改了Y(>w<)Y
    mtcnn = new MTCNN(tFaceModelDir);
    mtcnn->SetMinFace(40);
    mtcnn->SetNumThreads(2);
    facenet = new FaceNet(tFaceModelDir);
    facenet->SetThreadNum(4);
    liveness = new Liveness(tFaceModelDir);

    env->ReleaseStringUTFChars(faceDetectionModelPath_, faceDetectionModelPath);
    detection_sdk_init_ok = true;
    tRet = true;
    return initCorrect;
}

//TODO: FaceDetect 人脸检测方法
JNIEXPORT jintArray JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_FaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                    jint imageWidth, jint imageHeight, jint imageChannel) {
    //  LOGD("JNI开始检测人脸");
    if(!detection_sdk_init_ok){
        LOGE("人脸检测MTCNN模型SDK未初始化，直接返回空");
        return NULL;
    }

    int tImageDateLen = env->GetArrayLength(imageDate_);
    if(imageChannel == tImageDateLen / imageWidth / imageHeight){
        LOGE("数据宽=%d,高=%d,通道=%d",imageWidth,imageHeight,imageChannel);
    }
    else{
        LOGE("数据长宽高通道不匹配，直接返回空");
        return NULL;
    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);
    if (NULL == imageDate){
        LOGE("导入数据为空，直接返回空");
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    if(imageWidth<20||imageHeight<20){
        LOGE("导入数据的宽和高小于20，直接返回空");
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    //TODO 通道需测试
    if(3 == imageChannel || 4 == imageChannel){
        //图像通道数只能是3或4；
    }else{
        LOGE("图像通道数只能是3或4，直接返回空");
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    int32_t minFaceSize=80;
    mtcnn->SetMinFace(minFaceSize);

    unsigned char *faceImageCharDate = (unsigned char*)imageDate;
    ncnn::Mat ncnn_img;
    if(imageChannel==3) {
       ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_BGR2RGB,
                                                    imageWidth, imageHeight);
    }else{
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_RGBA2RGB, imageWidth, imageHeight);
    }

    std::vector<Bbox> finalBbox;
    // 开始MTCNN人脸检测
    mtcnn->detect(ncnn_img, finalBbox);

    int32_t num_face = static_cast<int32_t>(finalBbox.size());
    LOGE("检测到的人脸数目：%d\n", num_face);

    int out_size = 1+num_face*14;
    //  LOGD("内部人脸检测完成,开始导出数据");
    int *faceInfo = new int[out_size];
    faceInfo[0] = num_face;
    for(int i=0;i<num_face;i++){
        faceInfo[14*i+1] = finalBbox[i].x1;//left
        faceInfo[14*i+2] = finalBbox[i].y1;//top
        faceInfo[14*i+3] = finalBbox[i].x2;//right
        faceInfo[14*i+4] = finalBbox[i].y2;//bottom
        for (int j =0;j<10;j++){
            faceInfo[14*i+5+j]=static_cast<int>(finalBbox[i].ppoint[j]);
        }
    }

    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo,0,out_size,faceInfo);
    //  LOGD("内部人脸检测完成,导出数据成功");
    delete[] faceInfo;
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
    return tFaceInfo;
}


//TODO: MaxFaceDetect 检测最大人脸方法
JNIEXPORT jintArray JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_MaxFaceDetect(JNIEnv *env, jobject instance, jbyteArray imageDate_,
                                       jint imageWidth, jint imageHeight, jint imageChannel) {
    //  LOGD("JNI开始检测人脸");
    if(!detection_sdk_init_ok){
        LOGE("人脸检测MTCNN模型SDK未初始化，直接返回空");
        return NULL;
    }

    int tImageDateLen = env->GetArrayLength(imageDate_);
    if(imageChannel == tImageDateLen / imageWidth / imageHeight){
        LOGD("数据宽=%d,高=%d,通道=%d",imageWidth,imageHeight,imageChannel);
    }
    else{
        LOGD("数据宽=%d,高=%d,通道=%d",imageWidth,imageHeight,imageChannel);
        LOGE("数据长宽高通道不匹配，直接返回空");
        return NULL;
    }

    jbyte *imageDate = env->GetByteArrayElements(imageDate_, NULL);
    if (NULL == imageDate){
        LOGE("导入数据为空，直接返回空");
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    if(imageWidth<20||imageHeight<20){
        LOGE("导入数据的宽和高小于20，直接返回空");
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    //TODO 通道需测试
    if(3 == imageChannel || 4 == imageChannel){
        //图像通道数只能是3或4；
    }else{
        LOGE("图像通道数只能是3或4，直接返回空");
        env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
        return NULL;
    }

    int32_t minFaceSize=40;
    mtcnn->SetMinFace(minFaceSize);

    unsigned char *faceImageCharDate = (unsigned char*)imageDate;
    ncnn::Mat ncnn_img;
    if(imageChannel==3) {
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_BGR2RGB,
                                          imageWidth, imageHeight);
    }else{
        ncnn_img = ncnn::Mat::from_pixels(faceImageCharDate, ncnn::Mat::PIXEL_RGBA2RGB, imageWidth, imageHeight);
    }

    std::vector<Bbox> finalBbox;
    mtcnn->detectMaxFace(ncnn_img, finalBbox);

    int32_t num_face = static_cast<int32_t>(finalBbox.size());
    LOGE("检测到的人脸数目：%d\n", num_face);

    int out_size = 1+num_face*14;
    //  LOGD("内部人脸检测完成,开始导出数据");
    int *faceInfo = new int[out_size];
    faceInfo[0] = num_face;
    for(int i=0;i<num_face;i++){
        faceInfo[14*i+1] = finalBbox[i].x1;//left
        faceInfo[14*i+2] = finalBbox[i].y1;//top
        faceInfo[14*i+3] = finalBbox[i].x2;//right
        faceInfo[14*i+4] = finalBbox[i].y2;//bottom
        for (int j =0;j<10;j++){
            faceInfo[14*i+5+j]=static_cast<int>(finalBbox[i].ppoint[j]);
        }
    }

    jintArray tFaceInfo = env->NewIntArray(out_size);
    env->SetIntArrayRegion(tFaceInfo,0,out_size,faceInfo);
      LOGE("内部人脸检测完成,导出数据成功");
    delete[] faceInfo;
    env->ReleaseByteArrayElements(imageDate_, imageDate, 0);
    return tFaceInfo;
}

//TODO:LivenessDetected 活体检测方法
JNIEXPORT jboolean JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_LivenessDetecte(JNIEnv *env, jobject instance,
                                                    jbyteArray faceData_, jint w, jint h){
      LOGD("JNI开始检测活体");
    int tImageDateLen = env->GetArrayLength(faceData_);
    //Recognize: 检测人脸相关数据是否存在
    if (NULL==faceData_){
        LOGE("活体数据不存在");
        return false;
    }
    jbyte  *faceData = env->GetByteArrayElements(faceData_, NULL);
    if (NULL == faceData){
        LOGE("导入数据为空，直接返回空");
        env->ReleaseByteArrayElements(faceData_, faceData, 0);
        return NULL;
    }
    unsigned char *faceImageCharData = (unsigned char*) faceData;
    ncnn::Mat ncnn_img;
    if (tImageDateLen/w/h==4){
        ncnn_img = ncnn::Mat::from_pixels_resize(faceImageCharData, ncnn::Mat::PIXEL_RGBA2GRAY, w, h, 224, 224);
    } else{
        ncnn_img = ncnn::Mat::from_pixels_resize(faceImageCharData, ncnn::Mat::PIXEL_RGB2GRAY, w, h, 224, 224);
    }

    std::vector<float> cls_scores;
    liveness->Liveness_googlenet(ncnn_img, cls_scores);
    LOGE("活体检测结果==============");
    for (int i = 0; i < cls_scores.size(); ++i) {
        LOGE("活体结果：label:%d-->%f", i, cls_scores[i]);
    }
    LOGE("活体检测结果==============");

    if(cls_scores[1]>0.7){
        return true;
    } else{
        return false;
    }

}

//TODO: FaceDetectionModelUnInit 人脸检测模型释放方法
JNIEXPORT jboolean JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_FaceModelUnInit(JNIEnv *env, jobject instance) {
    if(!detection_sdk_init_ok){
        LOGE("人脸检测MTCNN模型已经释放过或者未初始化");
        return true;
    }
    jboolean tDetectionUnInit = false;
    delete mtcnn;


    detection_sdk_init_ok=false;
    tDetectionUnInit = true;
    LOGE("人脸检测初始化锁，重新置零");
    return tDetectionUnInit;

}

//TODO:SetMinFaceSize 设置最小人脸面积大小方法
JNIEXPORT jboolean JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_SetMinFaceSize(JNIEnv *env, jobject instance, jint minSize) {
    if(!detection_sdk_init_ok){
        LOGE("人脸检测MTCNN模型SDK未初始化，直接返回");
        return false;
    }

    if(minSize<=20){
        minSize=20;
    }

    mtcnn->SetMinFace(minSize);
    return true;
}

//TODO:SetThreadsNumber 设置线程数方法 4线程效果最好
JNIEXPORT jboolean JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_SetThreadsNumber(JNIEnv *env, jobject instance, jint threadsNumber) {
    if(!detection_sdk_init_ok){
        LOGE("人脸检测MTCNN模型SDK未初始化，直接返回");
        return false;
    }

    if(threadsNumber!=1&&threadsNumber!=2&&threadsNumber!=4&&threadsNumber!=8){
        LOGE("线程只能设置1，2，4，8");
        return false;
    }

    mtcnn->SetNumThreads(threadsNumber);
    return  true;
}

//TODO: SetTimeCount 设置循环测试次数
JNIEXPORT jboolean JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_SetTimeCount(JNIEnv *env, jobject instance, jint timeCount) {

    if(!detection_sdk_init_ok){
        LOGE("人脸检测MTCNN模型SDK未初始化，直接返回");
        return false;
    }

    mtcnn->SetTimeCount(timeCount);
    return true;

}

//TODO: FaceAlign 人脸对齐方法 正脸
JNIEXPORT jstring JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_FaceAlign(JNIEnv *env, jobject obj, jlong frame_, jfloatArray landmarks_){
       cv::Mat *frame = (cv::Mat*)frame_;
       //LOGD("input face size: %d, %d", frame.cols, frame.rows);
       std::vector<float> landmarks;
       float* marks = env->GetFloatArrayElements(landmarks_, NULL);
       int marks_num = env->GetArrayLength(landmarks_);
       for(int i =0;i<marks_num;i++){
            landmarks.push_back(marks[i]);
       }

       //LOGD("读入landmarks, 大小: %d", landmarks.size());
       env->ReleaseFloatArrayElements(landmarks_, marks, 0);
       pair<cv::Mat, string> aligned = aligner->align(*frame, landmarks);
       const char* pos = aligned.second.c_str();
       LOGE("face position: %s", pos);
       frame->create(aligned.first.rows, aligned.first.cols, aligned.first.type());
       memcpy(frame->data, aligned.first.data, aligned.first.rows * aligned.first.step);
       LOGE("frame after align: %d, %d", frame->cols, frame->rows);
       return env->NewStringUTF(aligned.second.c_str());
}

//TODO: FaceArray 返回人脸三个位置之一的128D向量方法
JNIEXPORT jfloatArray JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_FaceArray(JNIEnv *env, jobject obj, jbyteArray faceData_){
    jbyte *faceData = env->GetByteArrayElements(faceData_, NULL);
    unsigned char *faceImageCharData = (unsigned char*) faceData;
    int data_length = env->GetArrayLength(faceData_);
    LOGE("start get array--length: %d", data_length);
    ncnn::Mat ncnn_img = ncnn::Mat::from_pixels_resize(faceImageCharData, ncnn::Mat::PIXEL_RGBA2RGB, 160, 160, 112, 112);
    std::vector<float> feature;
    facenet->start(ncnn_img, feature);
    env->ReleaseByteArrayElements(faceData_, faceData, 0);
    jfloatArray res = env->NewFloatArray(feature.size());
    float *feature_arr = feature.data();
    env->SetFloatArrayRegion(res, 0, feature.size(), feature_arr);
    return res;
}

//TODO: FaceCompare 人脸比对方法
JNIEXPORT jdouble JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_FaceCompare(JNIEnv *env, jobject instance,
                                                    jbyteArray faceDate1_, jint w1, jint h1,
                                                    jbyteArray faceDate2_, jint w2, jint h2) {
    jbyte *faceDate1 = env->GetByteArrayElements(faceDate1_, NULL);
    jbyte *faceDate2 = env->GetByteArrayElements(faceDate2_, NULL);

    // TODO
    double similar=0;
    unsigned char *faceImageCharDate1 = (unsigned char*)faceDate1;
    unsigned char *faceImageCharDate2 = (unsigned char*)faceDate2;

    ncnn::Mat ncnn_img1 = ncnn::Mat::from_pixels_resize(faceImageCharDate1, ncnn::Mat::PIXEL_RGBA2RGB, w1, h1,112,112);
    ncnn::Mat ncnn_img2 = ncnn::Mat::from_pixels_resize(faceImageCharDate2, ncnn::Mat::PIXEL_RGBA2RGB, w2, h2,112,112);
    std::vector<float> feature1,feature2;

    facenet->start(ncnn_img1, feature1);
    facenet->start(ncnn_img2, feature2);

    env->ReleaseByteArrayElements(faceDate1_, faceDate1, 0);
    env->ReleaseByteArrayElements(faceDate2_, faceDate2, 0);

    similar = facenet->calSimilarity(feature1, feature2);

    LOGE("similar = %f", similar);
    return similar;
}

//TODO: AddFace 添加人脸方法
JNIEXPORT jboolean JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_AddFace(JNIEnv *env, jobject instance,
                                               jbyteArray faceDate_, jint w, jint h,
                                               jstring faceFeaturePath_, jstring id_){


    jbyte  *faceDate = env->GetByteArrayElements(faceDate_, NULL);
    unsigned char *faceImageCharDate = (unsigned char*) faceDate;
    ncnn::Mat ncnn_img = ncnn::Mat::from_pixels_resize(faceImageCharDate, ncnn::Mat::PIXEL_RGBA2RGB, w, h, 112,112);
    std::vector<float> feature;

    //TODO: AddFace提取人脸特征值
    facenet->start(ncnn_img, feature);

    //TODO: 写人脸特征文件到指定路径
    //获取人脸特征文件的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    int featureLenth = feature.size();
//    float f[featureLenth];
    float f[featureLenth];
    const char *faceFeaturePath = env->GetStringUTFChars(faceFeaturePath_, 0);
    const char *idName = env->GetStringUTFChars(id_, 0);

    if (NULL == faceFeaturePath){
        LOGE("人脸特征文件夹路径有误");
        return false;
    }
    if (NULL == idName){
        LOGE("ID有误");
        return false;

    }

    string id = idName;
    string faceFeatureDirPath = faceFeaturePath;
    string featurePath = faceFeatureDirPath.substr(faceFeatureDirPath.length()-1, 1);

    //目录补齐
    if("\\" == featurePath){
        faceFeatureDirPath = faceFeatureDirPath.substr(0, faceFeatureDirPath.length()-1) + "/";
    } else if (featurePath != "/"){
        faceFeatureDirPath = "/";
    }

    std::ofstream ofs(faceFeatureDirPath+id+".bin", std::ios::binary | std::ios::out);
    for (int i = 0; i < featureLenth; ++i) {
        f[i] = feature[i];
    }
        ofs.write( (const char*)f, sizeof(float) * featureLenth);

    ofs.close();

    env->ReleaseByteArrayElements(faceDate_, faceDate, 0);

    return true;
}

//TODO: Recognize 人脸识别方法
JNIEXPORT jstring JNICALL
Java_com_charleyszc_faceDemo_mobilefacenet_FaceEngine_Recognize(JNIEnv *env, jobject instance,
                                                    jbyteArray faceDate_, jint w, jint h,
                                                    jstring faceFeaturePath_, jdouble threshold){
    //显示人脸图宽高
    LOGE("W: %d", w);
    LOGE("h: %d", h);

    //人脸特征文件文件夹路径处理
    const char *faceFeaturePath = env->GetStringUTFChars(faceFeaturePath_, 0);

    //定义导入被识别人脸特征值变量
    std::vector<float> feature;
    //定义人脸比值
    double res;
    //人脸特征文件文件名数组
    vector<string> ids;


    //TODO: Recognize:人脸特征文件夹有问题  错误码：10000
    string facePathError = "10000";
    //TODO: Recognize:传入人脸值有问题  错误码：10001
    string faceDataError = "10001";
    //TODO: Recognize:人脸识别失败/查无此人  错误码：909090909090909
    string faceRecFault = "909090909090909";

    //保存idList取到的文件名缓存
    string id;
    //人脸特征文件路径缓存
    string idPath;
    //最大比值下标缓存
    int listId;

    //Recognize: 检测人脸相关数据是否存在
    if (NULL==faceDate_){
        LOGE("人脸数据不存在");
        return env->NewStringUTF(faceDataError.c_str());
    }
    //人脸特征文件不存在
    if(NULL == faceFeaturePath_){
        LOGE("人脸特征文件不存在");
        return env->NewStringUTF(facePathError.c_str());
    }

    //TODO: Recognize:对传入的人脸进行处理
    jbyte  *faceDate = env->GetByteArrayElements(faceDate_, NULL);
    unsigned char *faceImageCharDate = (unsigned char*) faceDate;
    ncnn::Mat ncnn_img = ncnn::Mat::from_pixels_resize(faceImageCharDate, ncnn::Mat::PIXEL_RGBA2RGB, w, h, 112, 112);

    //TODO: Recognize:计算人脸特征值存入feature
    facenet->start(ncnn_img, feature);
    env->ReleaseByteArrayElements(faceDate_, faceDate, 0);//释放

    //featureLenth 人脸特征值长度
    int featureLenth = feature.size();
    //f 人脸特征文件读取缓存
    float f[featureLenth];

    //人脸特征文件路径经过jstring2string转为string格式并保存
    std::string dirPath = jstring2string(env, faceFeaturePath_);
    //ids 保存人脸特征文件文件名集合
    //调用showAllId 遍历出人脸特征文件夹下全部人脸特征文件的文件名
    ids = showAllId(dirPath);
//    int fileNum = ids.size(); //索引到的全部人脸特征文件数

    //id列表
    std::vector<std::string> idList;
    //比对结果列表
    std::vector<double> resList;

    //识别结果ID
    string resultId;
    //存放单个人脸特征文件的人脸特征值
    std::vector<float> featureIn;
    LOGE("ids: %d", ids.size()); //显示id列表长度（本地人脸特征文件夹下文件个数）

    //获取id存入idList
    for (int i = 0; i < ids.size(); ++i) {
        idList.push_back(ids[i]);
    }

    //TODO: Recognize: 人脸识别主要部分
    for (int i = 0; i < ids.size(); ++i) {

        //获取当前人脸特征文件路径
        idPath = faceFeaturePath+ids[i];
        //定义输入流
        std::ifstream ifs;
        //打开并读取人脸特征文件
        ifs.open(idPath, std::ios::binary | std::ios::in );
        ifs.read( (char *)f, sizeof(float) * featureLenth);
        ifs.close();//关闭释放

        //TODO: Recognize:将读取到的人脸特征值保存
        for (int j = 0; j < featureLenth;  ++j) {
            featureIn.push_back(f[j]);
        }

        //TODO: Recognize:保存比值
        res = facenet->calSimilarity(feature,featureIn);
        featureIn.clear();
        //归一化处理，比值低于0.3的统一赋值为0.01
        if(res < 0.05){
            res = 0.01;
            //当前比值存入比值列表
            resList.push_back(res);
        } else{
            //当前比值存入比值列表
            resList.push_back(res);
        }
        //打印当前id和比值
        LOGE("listId: %d; res: %f",i,res);
    }
    //释放人脸特征值路径
    env->ReleaseStringUTFChars(faceFeaturePath_, faceFeaturePath);

    //保存最大比值的下标
    listId = Sort(resList, threshold);

    //如果下标数溢出 则判定人脸识别失败 给resultId传入错误码
    //否则将下标正常传入idList并返回相应的人脸特征文件名
    if (listId > resList.size()){
        resultId = faceRecFault;
        LOGE("识别人脸失败，可能是因为此人脸数据未被录入");
    } else {

        LOGE("listID: %d", listId);
        id = idList[listId];//返回相应的人脸特征文件名

        //定义string输入流
        istringstream iss(id);
        //将iss中的字符串通过'.'分割，传回'.'前的字符串（人脸id）
        getline(iss, resultId,'.');
        //打印id
//        std::cout<<"PERSON ID: "<<resultId<<endl;
        iss.clear();//释放输入流
    }
    //返回ID
    return env->NewStringUTF(resultId.c_str());
}

}
