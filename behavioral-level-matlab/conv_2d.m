function feature = conv_2d(img, weight, bias, H, V, N, M, K, S_H, S_V, P_H, P_V)
%conv_2d - 卷积
%
% Syntax: feature = conv_2d(img, weight, bias, H, V, N, M, K, S_H, S_V, P_H, P_V)
%
% img:      输入图像矩阵 [row, col, channel]
% weight:   权重矩阵 [row, col, channel, depth]
% bias:     偏置 [1, 1, depth]
% H:        输入矩阵水平方向的长度
% V:        输入矩阵垂直方向的长度
% N:        输入通道数量
% M:        输出通道数量
% K:        卷积核大小
% S_H:      水平方向的步长
% S_V:      垂直方向的步长
% P_H:      水平方向的填充
% P_V:      垂直方向的填充

    % 计算输出矩阵大小
    H_O_t = (H - K + 2*P_H)/S_H + 1;
    V_O_t = (V - K + 2*P_V)/S_V + 1;

    % H_O_t = (H - K)/S_H + 1;
    % V_O_t = (V - K)/S_V + 1;
    H_O = int32(H_O_t);
    V_O = int32(V_O_t);

    if H_O_t ~= H_O || V_O_t ~= V_O
        error('The size of the output matrix is not an integer!');
    end

    % 输出矩阵
    feature = zeros(V_O, H_O, M);

    % % 带 padding
    % for mm = 1:M
    %     for rr = 1:V_O
    %         for cc = 1:H_O
    %             tmp = 0;
    %             for nn = 1:N
    %                 for ii = 1:K
    %                     for jj = 1:K
    %                         ri = (rr-1)*S_V+1-P_V;
    %                         ci = (cc-1)*S_H+1-P_H;
    %                         rii = int32(ri+ii-1);
    %                         cii = int32(ci+jj-1);

    %                         if (cii == 0 || cii > H) || (rii == 0 || rii > V)
    %                             img_data = 0;
    %                         else
    %                             img_data = img(rii, cii, nn);
    %                         end

    %                         tmp = tmp + img_data * weight(ii, jj, nn, mm);
    %                     end
    %                 end
    %             end
    %             feature(rr, cc, mm) = tmp + bias(1 ,1 , mm);
    %         end
    %     end
    % end

    % 不带 padding, 矩阵乘法
    for mm = 1:M
        for rr = 1:V_O
            for cc = 1:H_O
                ri = (rr-1)*S_V+1;
                ci = (cc-1)*S_H+1;
                img_matrix = img(ri:1:ri+K-1, ci:1:ci+K-1, :);
                w_matrix = weight(:, :, :, mm);
                tmp = sum(sum(sum(img_matrix.*w_matrix)));
                feature(rr, cc, mm) = tmp + bias(1 ,1 , mm);
            end
        end
    end

    % fi 定点化
    feature = sfi(feature, 16, 11);
    
end
