package com.cyn.onlinelearning.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.media.mapper.MediaFilesMapper;
import com.cyn.onlinelearning.media.model.dto.QueryMediaParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileResultDto;
import com.cyn.onlinelearning.media.model.po.MediaFiles;
import com.cyn.onlinelearning.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
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
            folder = generateFileFolder(true, true, true);
        } else if (folder.endsWith("/")) {
            // 如果folder不是以 / 结尾需要追加 /
            folder = folder + "/";
        }
        // 根据字节数组的进行md5加密后生成一个唯一的值
        String md5 = DigestUtils.md5Hex(fileData);
        if (StringUtils.isBlank(objectName)) {
            // md5值  再拼接 fileName.lastIndexOf(".")即 文件后缀
            objectName = folder + md5 + uploadFileParamsDto.getFilename().substring(uploadFileParamsDto.getFilename().lastIndexOf("."));
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileData);
            String contentType = uploadFileParamsDto.getContentType();

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket_files)
                    // steam,
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .object(objectName)
                    .contentType(contentType)
                    .build();
            // 上传到MinIO
            minioClient.putObject(putObjectArgs);
            // 保存到数据库
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
            // 准备返回数据
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        } catch (Exception exception) {
            log.debug("上传文件失败:{}", exception.getMessage());
        }
        return null;
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
