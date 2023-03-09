package com.cyn.onlinelearning.media.api;


import com.cyn.onlinelearning.base.exception.OnlineLearningException;
import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.base.model.RestResponse;
import com.cyn.onlinelearning.media.model.dto.QueryMediaParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileResultDto;
import com.cyn.onlinelearning.media.model.po.MediaFiles;
import com.cyn.onlinelearning.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);
    }

    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestParam("filedata") MultipartFile fileData,
                                      @RequestParam(value = "folder",required = false) String folder,
                                      @RequestParam(value = "objectName",required = false) String objectName) {
        Long companyId = 123L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileData.getOriginalFilename());
        uploadFileParamsDto.setFileSize(fileData.getSize());
        uploadFileParamsDto.setContentType(fileData.getContentType());
        if (fileData.getContentType().contains("image")) {
            // 是图片
            uploadFileParamsDto.setFileType("001001");
        } else {
            // 是文本
            uploadFileParamsDto.setFileType("001003");
        }
        UploadFileResultDto uploadFileResultDto = null;
        try {
            uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamsDto, fileData.getBytes(), folder, objectName);
        } catch (IOException e) {
            OnlineLearningException.cast("上传文件异常");
        }
        return uploadFileResultDto;
    }
    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){

        //调用service查询文件的url

        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        return RestResponse.success(mediaFiles.getUrl());
    }

}
