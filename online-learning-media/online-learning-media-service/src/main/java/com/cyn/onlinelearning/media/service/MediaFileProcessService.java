package com.cyn.onlinelearning.media.service;

import com.cyn.onlinelearning.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/23 22:49
 */
public interface MediaFileProcessService {
    /**
     * 获取任务列表
     * @param shardTotal 总分片数量
     * @param shardIndex 当前执行器的索引位置
     * @param count 处理最大条数
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count);
}
