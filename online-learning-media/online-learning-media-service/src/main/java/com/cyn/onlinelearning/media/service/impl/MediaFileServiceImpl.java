package com.cyn.onlinelearning.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyn.onlinelearning.base.exception.OnlineLearningException;
import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.base.model.RestResponse;
import com.cyn.onlinelearning.media.mapper.MediaFilesMapper;
import com.cyn.onlinelearning.media.model.dto.QueryMediaParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileResultDto;
import com.cyn.onlinelearning.media.model.po.MediaFiles;
import com.cyn.onlinelearning.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Transactional
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MinioClient minioClient;
    @Value("${minio.bucket.files}")
    private String bucket_files;
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename())
                .eq(StringUtils.isNotBlank(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;
    }

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
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] fileData, String folder, String objectName) {
        if (StringUtils.isBlank(folder)) {
            // 如果没有folder 生成一个folder
            folder = generateFileFolder();
        } else if (folder.endsWith("/")) {
            // 如果folder不是以 / 结尾需要追加 /
            folder = folder + "/";
        }
        // 根据字节数组的进行md5加密后生成一个唯一的值
        String md5 = DigestUtils.md5Hex(fileData);
        if (StringUtils.isBlank(objectName)) {
            // md5值  再拼接 fileName.lastIndexOf(".")即 文件后缀
            objectName = md5 + uploadFileParamsDto.getFilename().substring(uploadFileParamsDto.getFilename().lastIndexOf("."));
        }
        objectName = folder + objectName;
        try {
            addMediaFile2MinIO(bucket_files, fileData, objectName);
            // 保存到数据库
            MediaFiles mediaFiles = addMediaInfo2DB(companyId, uploadFileParamsDto, objectName, md5);
            // 准备返回数据
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        } catch (Exception exception) {
            log.debug("上传文件失败:{}", exception.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileMd5
     * @return true 存在 false 不存在
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 判断文件存在标注: 在数据库中有记录，且在文件表中存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        boolean isExistInDB = mediaFiles != null;
        boolean isExistInMinIO;
        if (isExistInDB) {
            GetObjectArgs getObjArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build();
            try {
                InputStream stream = minioClient.getObject(getObjArgs);
                isExistInMinIO = stream == null;
                return RestResponse.success(isExistInMinIO);
            } catch (Exception exception) {
                exception.printStackTrace();
                return RestResponse.success(false);
            }
        } else {
            return RestResponse.success(false);
        }
    }

    /**
     * 检查分块是否存在
     *
     * @param fileMd5
     * @param chunk
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunk) {
        // 获取分块文件的目录
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        // 获取分块文件最终路径
        String chunkPath = chunkFolderPath + chunk;
        // 查询文件按系统
        GetObjectArgs getObjArgs = GetObjectArgs.builder()
                .bucket(bucket_videofiles)
                .object(chunkPath)
                .build();
        try {
            InputStream stream = minioClient.getObject(getObjArgs);
            return RestResponse.success(stream == null);
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.success(false);
        }
    }

    /**
     * 上传分块文件
     *
     * @param fileMd5 md5后的文件名
     * @param chunk   分块序号
     * @param bytes   分块文件的字节数组
     * @return
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
        //得到分块文件所在目录
        String chunkFileFolderPath = getChunkFolderPath(fileMd5);
        //分块文件的路径
        String chunkPath = chunkFileFolderPath + chunk;
        try {
            //将分块上传到文件系统
            addMediaFile2MinIO(bucket_videofiles, bytes, chunkPath);
            //上传成功
            return RestResponse.success(true);
        } catch (Exception e) {
            log.debug("上传分块文件失败：{}", e.getMessage());
            return RestResponse.validfail(false, "上传分块失败");
        }
    }

    // ========================= private methods =========================

    /**
     * 获取分块文件所在的路径
     *
     * @param fileMd5
     * @return
     */
    private String getChunkFolderPath(String fileMd5) {
        return fileMd5.charAt(0) +
                "/" +
                fileMd5.charAt(1) +
                "/" +
                "chunk" +
                "/";
    }

    /**
     * 将文件信息插入到 online-learning_media.mediaFiles表中
     *
     * @param companyId
     * @param uploadFileParamsDto
     * @param objectName
     * @param md5
     * @return
     */
    @NotNull
    private MediaFiles addMediaInfo2DB(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String md5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(md5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            // 封装数据
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(md5);
            mediaFiles.setFileId(md5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket_files);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/" + bucket_files + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            // 插入文件列表
            mediaFilesMapper.insert(mediaFiles);
        }
        return mediaFiles;
    }

    /**
     * 将文件上传至分布式文件系统MinIO
     *
     * @param bucketName
     * @param fileData
     * @param objectName
     */
    private void addMediaFile2MinIO(String bucketName, byte[] fileData, String objectName) {
        try {
            // 设置默认的contentType
            String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            // 取objectName的扩展名
            if (objectName.contains(".")) {
                //
                ContentInfo extension = ContentInfoUtil.findExtensionMatch(objectName.substring(objectName.lastIndexOf(".")));
                if (extension != null) {
                    contentType = extension.getMimeType();
                }
            }
            // 将文件的byte数组转化为输入流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    // steam,
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .object(objectName)
                    .contentType(contentType)
                    .build();
            // 上传到MinIO
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            log.error("上传到文件系统出错:{}", e.getMessage());
            OnlineLearningException.cast("上传文件出错");
        }
    }

    /**
     * 默认需要路径全生成
     * @return
     */
    private String generateFileFolder(){
        return this.generateFileFolder(true,true,true);
    }
    /**
     * 生成默认路径 year/month/day
     *
     * @param year  是否需要年
     * @param month 是否需要月
     * @param day   是否需要日
     * @return
     */
    private String generateFileFolder(boolean year, boolean month, boolean day) {
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String format = now.format(dateTimeFormatter);
        String[] split = format.split("/");
        StringBuilder builder = new StringBuilder();
        if (year) {
            builder.append(split[0]);
            builder.append("/");
        }
        if (month) {
            builder.append(split[1]);
            builder.append("/");
        }
        if (day) {
            builder.append(split[2]);
            builder.append("/");
        }
        return builder.toString();
    }
}
