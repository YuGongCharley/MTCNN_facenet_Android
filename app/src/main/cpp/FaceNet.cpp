//
// Created by SongZichen on 2019-05-13.
//
#include "FaceNet.h"
#include <iostream>
#include <mat.h>
#include <net.h>

namespace std {
	FaceNet::FaceNet(const string &model_path) {

//		string param_files = model_path + "/mobilefacenet.param";
//		string bin_files = model_path + "/mobilefacenet.bin";

        string param_files = model_path + "/recognition1.param";
        string bin_files = model_path + "/recognition1.bin";
		net.load_param(param_files.c_str());
		net.load_model(bin_files.c_str());
	}

	FaceNet::~FaceNet() {
		net.clear();
	}

	void FaceNet::start(ncnn::Mat &ncnn_img, vector<float> &feature128) {
		RecogNet(ncnn_img);
		feature128 = feature_out;
	}

	void FaceNet::SetThreadNum(int threadNum) {
		threadNum = threadNum;
	}

	void FaceNet::RecogNet(ncnn::Mat &img_) {
		feature_out.resize(128);
		ncnn::Extractor extractor = net.create_extractor();
		extractor.set_num_threads(threadNum);
		extractor.set_light_mode(true);
		extractor.input("data", img_);
		ncnn::Mat out;
		extractor.extract("fc1", out);
		for (int j = 0; j < 128; j++) {
			feature_out[j] = out[j];
		}
	}

	double FaceNet::calSimilarity(vector<float> &v1, vector<float> &v2) {
		if (v1.size() != v2.size() || !v1.size())
			return 0;
		double ret = 0.0, mod1 = 0.0, mod2 = 0.0, square = 0.0, result = 0.0;
//		double square = 0.0, result = 0.0;
		for (std::vector<double>::size_type i = 0; i != v1.size(); ++i) {
		ret += v1[i] * v2[i];
		mod1 += v1[i] * v1[i];
		mod2 += v2[i] * v2[i];

//			square += (v1[i] - v2[i]) * (v1[i] - v2[i]);


		}
//		result = sqrt(1- (square / v1.size()));
//  result = 1-sqrt((square/v1.size()));
	return ret / sqrt(mod1) / sqrt(mod2);

//    std::cout<<"人脸相似度："<<sqrt(1-square/v1.size())<<endl;
		return result;
	}
}

