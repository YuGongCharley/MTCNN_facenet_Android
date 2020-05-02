//
// Created by SongZichen on 2019-05-13.
//
#include "FaceAlign.h"

FaceAlign::FaceAlign(){}
FaceAlign::~FaceAlign(){}

/*
最开始传入的points中
0,5 左眼 1,6 右眼 
2,7 鼻子 3,8 左口 4,9 右口
*/
// 用于判断是左脸右脸还是中间脸，points[0] points[1] points[2] 之间的差的比例代表左右
string FaceAlign::getPos(const vector<float>& points) {
	if (abs(points[0] - points[2]) / abs(points[1] - points[2]) > 2)
		return "Right";
	else if (abs(points[1] - points[2]) / abs(points[0] - points[2]) > 2)
		return "Left";
	return "Center";
}

vector<float> FaceAlign::list2colmatrix(const vector<pair<float, float>>& pts_list)
{
	vector<float> colMat;
	for (int i = 0; i < pts_list.size(); i++) {
		colMat.push_back(pts_list[i].first);
		colMat.push_back(pts_list[i].second);
	}
	return colMat;
}

vector<pair<float, float>> vectorTo2Dmatrix(const vector<float> &vec) {
	vector<pair<float, float>> res;
	for (int i = 0; i < vec.size(); i+=2) {
		res.push_back({ vec[i], vec[i + 1] });
	}
	return res;
}

pair<cv::Mat, cv::Mat> FaceAlign::find_tfrom_between_shapes(const vector<float> &from_shape, const vector<float>& to_shape)
{
	float sigma_from = 0.0;
	float sigma_to = 0.0;
	vector<pair<float, float>> from_shape_points = vectorTo2Dmatrix(from_shape);
	vector<pair<float, float>> to_shape_points = vectorTo2Dmatrix(to_shape);
	pair<float, float> mean_from;
	pair<float, float> mean_to;
	float tmp = 0.0;
	// 求mean_from土味矩阵均值
	for (int i = 0; i < from_shape_points.size(); i++)
		tmp += from_shape_points[i].first;
	tmp /= from_shape_points.size();
	mean_from.first = tmp;
	tmp = 0;
	for (int i = 0; i < from_shape_points.size(); i++)
		tmp += from_shape_points[i].second;
	tmp /= from_shape_points.size();
	mean_from.second = tmp;
	// 求mean_to土味均值
	tmp = 0;
	for (int i = 0; i < to_shape_points.size(); i++)
		tmp += to_shape_points[i].first;
	tmp /= to_shape_points.size();
	mean_to.first = tmp;
	tmp = 0;
	for (int i = 0; i < to_shape_points.size(); i++)
		tmp += to_shape_points[i].second;
	tmp /= to_shape_points.size();
	mean_to.second = tmp;

	// 五个人脸特征点泛化处理
	vector<pair<float, float>> cov = { {0.0, 0.0},{0.0, 0.0} }; // 对原图关键点进行卷积
	pair<float, float> from_dis;
	pair<float, float> to_dis;
	for (int i = 0; i < from_shape_points.size(); i++) {
		// 每个点的范数²
		from_dis.first = from_shape_points[i].first - mean_from.first;
		from_dis.second = from_shape_points[i].second - mean_from.second;
		sigma_from  += pow(from_dis.first, 2) + pow(from_dis.second, 2);
		to_dis.first = to_shape_points[i].first - mean_to.first;
		to_dis.second = to_shape_points[i].second - mean_to.second;
		sigma_to += pow(to_dis.first, 2) + pow(to_dis.second, 2);
		// 范数进行乘法添加卷积
		vector<pair<float, float>> temp = { {to_dis.first * from_dis.first, to_dis.first * from_dis.second},
											{to_dis.second * from_dis.first, to_dis.second * from_dis.second} };
		cov[0].first += temp[0].first; cov[0].second += temp[0].second; cov[1].first += temp[1].first; cov[1].second += temp[1].second;
	}
	sigma_from = sigma_from / to_shape_points.size();
	sigma_to = sigma_to / to_shape_points.size();
	cov[0].first = cov[0].first / to_shape_points.size(); cov[0].second = cov[0].second / to_shape_points.size(); 
	cov[1].first = cov[1].first / to_shape_points.size(); cov[1].second = cov[1].second / to_shape_points.size();
	
	// 计算仿射矩阵
	vector<pair<float, float>> s = { {1.0, 0.0},{0.0, 1.0} };
	// 对cov进行奇异值分解
	//d==sigma
	// d为奇异值， u为左奇异矩阵，vt为转置过的右奇异矩阵，opencv和numpy的实现都对，但符号有区别
	cv::Mat u, d, vt;
	cv::Mat src(2, 2, CV_32FC1);
	cv::Mat ss(2, 2, CV_32FC1);
	src.at<float>(0, 0) = cov[0].first;
	src.at<float>(0, 1) = cov[0].second;
	src.at<float>(1, 0) = cov[1].first;
	src.at<float>(1, 1) = cov[1].second;
	cv::SVD::compute(src, d, u, vt, 4);
	// 求cov的行列式
	float det = cv::determinant(src);
	if (det < 0) {
		if (d.at<float>(1, 0) < d.at<float>(0, 0))
			s[1].second = -1;
		else
			s[0].first = -1;
	}
	ss.at<float>(0, 0) = s[0].first;
	ss.at<float>(0, 1) = s[0].second;
	ss.at<float>(1, 0) = s[1].first;
	ss.at<float>(1, 1) = s[1].second;
	cv::Mat r = u * ss * vt;
	
	float c = 1.0;
	if (sigma_from != 0) {
		// np.diag 操作
		cv::Mat dd(2, 2, CV_32FC1);
		dd.at<float>(0, 0) = d.at<float>(0,0);
		dd.at<float>(0, 1) = 0.0;
		dd.at<float>(1, 0) = 0.0;
		dd.at<float>(1, 1) = d.at<float>(1,0);
		c = 1.0 / sigma_from * cv::trace(dd * ss).val[0];
	}
	
	cv::Mat from(2, 1, CV_32FC1);
	cv::Mat to(2, 1, CV_32FC1);
	from.at<float>(0, 0) = mean_from.first;
	from.at<float>(1, 0) = mean_from.second;
	to.at<float>(0, 0) = mean_to.first;
	to.at<float>(1, 0) = mean_to.second;
	cv::Mat tran_b = to - c * r * from;
	cv::Mat tran_m = c * r;
	return { tran_m, tran_b };
}

pair<cv::Mat, string> FaceAlign::align(cv::Mat & img, const vector<float>& landmarks, int desired_size, float padding)
{	
	vector<float> shape;
	for (int i = 0; i < 5; i++) {
		shape.push_back(landmarks[i]);
		shape.push_back(landmarks[i+5]);
	}
	if (padding < 0)
		padding = 0;
	vector<float> mean_face_shape_x = { 0.224152, 0.75610125, 0.490127, 0.254149, 0.726104 };
	vector<float> mean_face_shape_y = { 0.2119465, 0.2119465, 0.628106, 0.780233, 0.780233 };
	vector<pair<float, float>> from_points;
	vector<pair<float, float>> to_points;

	for (int i = 0; i < 5; i++) {
		float x = (padding + mean_face_shape_x[i]) / (2 * padding + 1) * desired_size;
		float y = (padding + mean_face_shape_y[i]) / (2 * padding + 1) * desired_size;
		to_points.push_back({ x, y });
		from_points.push_back({ shape[2 * i], shape[2 * i + 1] });
	}
	vector<float> from_mat = list2colmatrix(from_points);
	vector<float> to_mat = list2colmatrix(to_points);
	pair<cv::Mat, cv::Mat> tran = find_tfrom_between_shapes(from_mat, to_mat);
	cv::Mat tran_m = tran.first;
	cv::Mat tran_b = tran.second;
	cv::Mat probe_vec(2, 1, CV_32FC1);
	probe_vec.at<float>(0, 0) = 1.0;
	probe_vec.at<float>(1, 0) = 0.0;
	probe_vec = tran_m * probe_vec;

	// scale = np.linalg.norm(probe_vec) probe_vec的范数
	float scale = pow(pow(probe_vec.at<float>(0, 0), 2) + pow(probe_vec.at<float>(1, 0), 2), 0.5);
	const float PI = atan(1.0) * 4;
	float angle = 180.0 / PI * atan2(probe_vec.at<float>(1, 0), probe_vec.at<float>(0, 0));
	vector<float> from_center = { (shape[0] + shape[2]) / 2, (shape[1] + shape[3]) / 2 };
	vector<float> to_center = { 0.0, 0.0 };
	to_center[0] = desired_size * 0.5;
	to_center[1] = desired_size * 0.4;

	float ex = to_center[0] - from_center[0];
	float ey = to_center[1] - from_center[1];

	cv::Mat rot_mat(2, 3, CV_32FC1);
	rot_mat= getRotationMatrix2D(cv::Point2f(from_center[0], from_center[1]), -1 * angle, scale);
	
	// rot_mat 读写存在未知问题，debug四小时找不到原因，强行sstream分析
	std::ostringstream out;
	out << rot_mat;
	string str = out.str();
	std::istringstream parser(str);
	float alpha, beta;
	char st;
	parser >> st >> alpha >> st >> beta;
	cv::Mat rot(2, 3, CV_32FC1);
	rot.at<float>(0, 0) = alpha;
	rot.at<float>(0, 1) = beta;
	rot.at<float>(0, 2) = (1-alpha) * from_center[0] - beta * from_center[1];
	rot.at<float>(1, 0) = -beta;
	rot.at<float>(1, 1) = alpha;
	rot.at<float>(1, 2) = beta * from_center[0] + (1-alpha) * from_center[1];

	cv::Mat shift(2, 3, CV_32FC1);
	shift.at<float>(0, 0) = 0.0;
	shift.at<float>(0, 1) = 0.0;
	shift.at<float>(0, 2) = ex;
	shift.at<float>(1, 0) = 0.0;
	shift.at<float>(1, 1) = 0.0;
	shift.at<float>(1, 2) = ey;

	cv::Mat chips;
	cv::Size d_size(desired_size, desired_size);
	cv::warpAffine(img,chips, rot+shift, d_size);
//	cv::namedWindow("FaceAlign");
//	cv::imshow("FaceAlign", chips);
	return { chips, getPos(landmarks) };
}

