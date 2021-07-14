function feature = bn(img, scale_i, offset_i, H, V, N)
%bn - Batch Normalization
%
% Syntax: feature = bn(img, scale_i, offset_i, H, V, N)
%
% img:      输入图像矩阵 [row, col, channel]
% scale_i:  [1, 1, depth]
% offset_i: [1, 1, depth]
% H:        输入矩阵水平方向的长度
% V:        输入矩阵垂直方向的长度
% N:        输入通道数量

    % 输出矩阵
    feature = zeros(V, H, N);

    % 多层循环
    for nn = 1:N
        for rr = 1:V
            for cc = 1:H
                feature(rr, cc, nn) = img(rr, cc, nn)*scale_i(1,1,nn)+offset_i(1,1,nn);
            end
        end
    end

    % % 矩阵乘法
    % for nn = 1:N
    %     scale_data = scale_i(1,1,nn);
    %     offset_data = offset_i(1,1,nn);
    %     feature(:,:,nn) = img(:,:,nn)*scale_data+offset_data;
    % end

    % fi 定点化
    feature = sfi(feature, 16, 10);

end