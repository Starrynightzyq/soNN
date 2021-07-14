function feature = relu(img, H, V, N)
%relu - 激活函数
%
% Syntax: feature = relu(img, scale_i, offset_i, H, V, N)
%
% img:      输入图像矩阵 [row, col, channel]
% H:        输入矩阵水平方向的长度
% V:        输入矩阵垂直方向的长度
% N:        输入通道数量
    
    % 输出矩阵
    feature = zeros(V, H, N);

    for nn = 1:N
        for rr = 1:V
            for cc = 1:H
                if img(rr, cc, nn) < 0
                    feature(rr, cc, nn) = 0;
                else
                    feature(rr, cc, nn) = img(rr, cc, nn);
                end
            end
        end
    end

    % fi 定点化
    feature = sfi(feature, 16, 15);

end