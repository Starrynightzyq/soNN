function ofmap = pe_model(ifmap, filter, ichannel, ochannel, ifmapW, ifmapH, filterK, stepW, stepH)
%pe_model - Description
%
% Syntax: ofmap = pe_model(ifmap, filter, ichannel, ochannel, ifmapW, ifmapH, filterK, stepW, stepH)
%
% input:
% ifmap 输入图像
% filter 卷积核
% ichannel 输入图像的通道数
% ochannel 输出图像的通道数
% ifmapW 输入图像的宽度
% ifmapH 输入图像的高度
% filterK 卷积核的宽度（only 3）
% stepW 水平方向的步长
% stepH 垂直方向的步长
% output:
% ofmap 输出图像

    maxAddr = 100;

    % 输出图像的宽度/高度
    ofmapW = (ifmapW-filterK)/stepW + 1;
    ofmapH = (ifmapH-filterK)/stepH + 1;

    % 
    filterLen = filterK*ichannel*ochannel;
    ifmapLen = filterK*ichannel;

    % 部分和
    pSumRam = zeros(maxAddr);

    % addr
    addrCounter = 1;
    ichannelCounter = 1;
    % calCounter = 1;

    ifmapCounter = 1;
    filterCounter = 1;
    for calCounter = 1:ifmapLen
        pSumRam(addrCounter) = ifmap(ifmapCounter)*filter(filterCounter);

        if ichannelCounter == ichannel
            ichannelCounter = 1;
        else
            ichannelCounter = ichannelCounter+1;
        end

        if ichannelCounter == ichannel
            ifmapCounter = ifmapCounter + 1;
        end

        filterCounter = filterCounter + 1;
        
    end
    
end