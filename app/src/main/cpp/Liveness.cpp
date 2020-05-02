//
// Created by SongZichen on 2019-06-28.
//
#include "Liveness.h"
#include "include/mat.h"

#include <android/log.h>
#include <opencv2/core.hpp>

#define TAG "GoogleNetCpp"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

//构造函数 用于加载模型路径
Liveness::Liveness(const string &model_path) {

    string param_file = model_path+"/nir_nfd_gn7.param";
    string bin_file = model_path+"/nir_nfd_gn7.bin";
//    googlenet.use_int8_inference = 0;
//    googlenet.use_sgemm_convolution = 0;
//    googlenet.use_vulkan_compute = 0;
    googlenet.load_param(param_file.c_str());
    googlenet.load_model(bin_file.c_str());
}

Liveness::~Liveness() {
    googlenet.clear();
}

void Liveness::Liveness_googlenet(ncnn::Mat &img_, std::vector<float> &cls_scores) {
    img = img_;

    ncnn::Extractor exG = googlenet.create_extractor();
    exG.set_num_threads(4);
    exG.set_light_mode(true);

    exG.input("data", img_);

    ncnn::Mat out;
    exG.extract("softmax", out);

    cls_scores.resize(out.w);
    for (int i = 0; i < out.w; i++) {
        cls_scores[i] = out[i];
//        LOGE("活体检测结果==============");
//        printf("活体结果：label:%d-->%f", i, cls_scores[i]);
//
//        LOGE("活体检测结果==============");
    }

}



