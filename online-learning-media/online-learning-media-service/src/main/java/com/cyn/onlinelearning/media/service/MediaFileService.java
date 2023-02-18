package com.cyn.onlinelearning.media.service;


import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.media.model.dto.QueryMediaParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileResultDto;
import com.cyn.onlinelearning.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.cyn.onlinelearning.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件接口
  * @param companyId 机构id
  * @param uploadFileParamsDto 参数
  * @param fileData 文件本身(byte流)
  * @param folder 存放路径
  * @param objectName 文件名
  * @return
  */
 UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,byte[] fileData,String folder,String objectName);


}
