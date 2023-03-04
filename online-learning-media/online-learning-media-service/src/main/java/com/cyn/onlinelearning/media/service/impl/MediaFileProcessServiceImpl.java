package com.cyn.onlinelearning.media.service.impl;

import com.cyn.onlinelearning.media.mapper.MediaProcessHistoryMapper;
import com.cyn.onlinelearning.media.mapper.MediaProcessMapper;
import com.cyn.onlinelearning.media.model.po.MediaProcess;
import com.cyn.onlinelearning.media.model.po.MediaProcessHistory;
import com.cyn.onlinelearning.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/23 22:51
 */
@Service
@Transactional
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    private MediaProcessMapper mediaProcessMapper;
    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 获取任务列表
     *
     * @param shardTotal 总分片数量
     * @param shardIndex 当前执行器的索引位置
     * @param count      处理最大条数
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardTotal, int shardIndex, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMs) {
        // 查询任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            log.debug("当前任务为空:{}", taskId);
            return;
        }
        // 判断是成功还是失败
        if ("3".equals(status)) {
            mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(errorMs);
            // 如果失败了 需要更新两个字段
            mediaProcessMapper.updateField(mediaProcess);
            return;
        }
        // 如果处理成功将待处理表记录删除
        if ("2".equals(status)) {
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.updateField(mediaProcess);
        }
        // 如果处理成功需要将当前任务记录到历史任务表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        // 如果处理成功将待处理表的字段删除
        mediaProcessMapper.deleteById(mediaProcess);
    }
}
