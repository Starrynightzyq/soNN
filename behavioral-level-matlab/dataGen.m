clc, clear;

load('data.mat')

%% config
conv1_img_qbits = 8;
conv1_filter_qbits = 6;

H_c1 = 49;
V_c1 = 26;
N_c1 = 1;
M_c1 = 28;
K_c1 = 3;

%% conv1

img_c1_t(:,:,1) = conv1_in_fi8(:,:);
weight_c1_t = Conv1_W_fi8;
bias_c1_t = Conv1_b_fi8;

img_c1_tmp = img_c1_t*(2^conv1_img_qbits);
weight_c1_tmp = weight_c1_t*(2^conv1_filter_qbits);

img_c1_q = zeros(H_c1, V_c1, N_c1);

fprintf('val pics = DenseMatrix(\n');
for row = 1:V_c1
    fprintf('List(');
    for col = 1:H_c1
        tmp = img_c1_tmp(row, col, 1);
        fprintf('%d', tmp);
        if col ~= H_c1
            fprintf(', ');
        end
    end
    if row == V_c1
        fprintf(')\n');
    else 
        fprintf('),\n');
    end
end
fprintf(')\n');

fprintf('val flts1 = DenseVector(\n');
for channel = 1:M_c1
    fprintf('DenseMatrix(');
    for row = 1:K_c1
        fprintf('(');
        for col = 1:K_c1
            tmp = weight_c1_tmp(row, col, channel);
            fprintf('%d', tmp);
            if col ~= K_c1
                fprintf(', ');
            end
        end
        if row == K_c1
            fprintf(')');
        else 
            fprintf('),');
        end
    end
    if channel == M_c1
        fprintf(')\n');
    else 
        fprintf('),\n');
    end
end
fprintf(')\n');