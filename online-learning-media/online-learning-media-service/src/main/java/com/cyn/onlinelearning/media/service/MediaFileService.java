package com.cyn.onlinelearning.media.service;


import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.base.model.RestResponse;
import com.cyn.onlinelearning.media.model.dto.QueryMediaParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileResultDto;
import com.cyn.onlinelearning.media.model.po.MediaFiles;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.cyn.onlinelearning.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件接口
     *
     * @param companyId           机构id
     * @param uploadFileParamsDto 参数
     * @param fileData            文件本身(byte流)
     * @param folder              存放路径
     * @param objectName          文件名
     * @return
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] fileData, String folder, String objectName);


    /**
     * 检查文件是否存在
     * @param fileMd5
     * @return
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     * @param fileMd5
     * @param chunk
     * @return
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunk);

    /**
     * 上传分块文件
     * @param fileMd5 md5后的文件名
     * @param chunk 分块序号
     * @param bytes 分块文件的字节数组
     * @return
     */
    RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes);

    /**
     * 合并分片
     * @param companyId 机构id
     * @param fileMd5 文件md5值
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);
}
