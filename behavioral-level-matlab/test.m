clc, clear;

load('data.mat')

img_c1_t(:,:,1) = conv1_in_fi8(:,:);
weight_c1_t = Conv1_W_fi8;
bias_c1_t = Conv1_b_fi8;

weight_c2_t = Conv2_W_fi8;
bias_c2_t = Conv2_b_fi8;

% 第一层卷积
img_c1 = img_c1_t;
weight_c1 = weight_c1_t;
bias_c1 = bias_c1_t;
H_c1 = 49;
V_c1 = 26;
N_c1 = 1;
M_c1 = 28;
K_c1 = 3;
S_H_c1 = 2;
S_V_c1 = 1;
P_H_c1 = 0;
P_V_c1 = 0;

% 第一层 BN
scale_bn1 = scale1_fi8;
offset_bn1 = offset1_fi8;
H_bn1 = 24;
V_bn1 = 24;
N_bn1 = M_c1;

% 第一层激活
H_act1 = H_bn1;
V_act1 = V_bn1;
N_act1 = N_bn1;

% 第二层卷积
weight_c2 = weight_c2_t;
bias_c2 = bias_c2_t;
H_c2 = 24;
V_c2 = 24;
N_c2 = 28;
M_c2 = 24;
K_c2 = 3;
S_H_c2 = 1;
S_V_c2 = 1;
P_H_c2 = 0;
P_V_c2 = 0;

%%
% 第一层
feature_c1 = conv_2d(img_c1, weight_c1, bias_c1, H_c1, V_c1, N_c1, M_c1, K_c1, S_H_c1, S_V_c1, P_H_c1, P_V_c1);

feature_bn1 = bn(feature_c1, scale_bn1, offset_bn1, H_bn1, V_bn1, N_bn1);

feature_act1 = relu(feature_bn1, H_act1, V_act1, N_act1);

%%
% 第二层
feature_c2 = conv_2d(feature_act1, weight_c2, bias_c2, H_c2, V_c2, N_c2, M_c2, K_c2, S_H_c2, S_V_c2, P_H_c2, P_V_c2);
