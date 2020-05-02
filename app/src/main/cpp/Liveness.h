//
// Created by SongZichen on 2019-06-28.
//
#pragma once

#ifndef CHARLEYSZC_FACEDEMO_LIVENESS_H
#define CHARLEYSZC_FACEDEMO_LIVENESS_H

#include <mat.h>
#include <net.h>
#include <string>
#include <vector>
#include "net.h"
using namespace std;
class Liveness{

public:
    Liveness(const string &model_path);
    ~Liveness();
    void Liveness_googlenet(ncnn::Mat& img_, std::vector<float>& cls_scores);
private:
    ncnn::Mat img ;
//    ncnn::Mat img = ncnn::Mat(ncnn::Mat::PIXEL_RGB,224,224);
    ncnn::Net googlenet;
    const float mean_vals[3] = {104.f, 117.f, 123.f};
    int img_w, img_h;
};

#endif //CHARLEYSZC_FACEDEMO_LIVENESS_H
