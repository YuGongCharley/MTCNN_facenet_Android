//
// Created by SongZichen on 2019-05-13.
//
#pragma once
#ifndef FACENET_H_
#define FACENET_H_
#include "net.h"
#include <string>
#include <cstdio>
#include <algorithm>
namespace std{

class FaceNet {
	public:
		FaceNet(const string &model_path);
		~FaceNet();
		void start(ncnn::Mat& ncnn_img, vector<float>& feature128);
		void SetThreadNum(int threadNum);
		double calSimilarity(vector<float>& v1, vector<float>& v2);
	private:
		void RecogNet(ncnn::Mat& img_);
		ncnn::Net net;
		vector<float> feature_out;
		int threadNum = 4;
	};

}
#endif // !FACENET_H_
