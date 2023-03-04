package com.cyn.onlinelearning.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyn.onlinelearning.base.exception.OnlineLearningException;
import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.base.model.RestResponse;
import com.cyn.onlinelearning.media.mapper.MediaFilesMapper;
import com.cyn.onlinelearning.media.mapper.MediaProcessMapper;
import com.cyn.onlinelearning.media.model.dto.QueryMediaParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileParamsDto;
import com.cyn.onlinelearning.media.model.dto.UploadFileResultDto;
import com.cyn.onlinelearning.media.model.po.MediaFiles;
import com.cyn.onlinelearning.media.model.po.MediaProcess;
import com.cyn.onlinelearning.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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
                .eq(StringUtils.isNotBlank(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType())
                .orderByDesc(MediaFiles::getCreateDate);

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
            MediaFiles mediaFiles = addMediaInfo2DB(companyId, uploadFileParamsDto, objectName, md5, bucket_files);
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

    /**
     * 合并分片
     *
     * @param companyId           机构id
     * @param fileMd5             文件md5值
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return
     */
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 下载分片
        File[] chunks = checkChunkStatus(fileMd5, chunkTotal);
        File tempMergeFile = null;
        try {
            // 合并分块

            String filename = uploadFileParamsDto.getFilename();
            String extension = filename.substring(filename.indexOf("."));
            try {
                tempMergeFile = File.createTempFile("merge", extension);
            } catch (IOException e) {
                e.printStackTrace();
                OnlineLearningException.cast("创建合并临时文件出错");
            }
            // 创建合并文件流对象
            try {
                RandomAccessFile rw = new RandomAccessFile(tempMergeFile, "rw");
                byte[] bytes = new byte[1024];
                for (File chunk : chunks) {
                    RandomAccessFile r = new RandomAccessFile(chunk, "r");
                    int len = -1;
                    while ((len = r.read(bytes)) != -1) {
                        rw.write(bytes, 0, len);
                    }
                }
            } catch (IOException e) {
                OnlineLearningException.cast("合并文件出错");
            }
            // 校验合并后文件是否正确
            try {
                FileInputStream mergeInputStream = new FileInputStream(tempMergeFile);
                String md5Hex = DigestUtils.md5Hex(mergeInputStream);
                if (!fileMd5.equals(md5Hex)) {
                    log.error("合并文件校验失败,文件路径:{} 原始文件MD5:{}", tempMergeFile.getAbsolutePath(), fileMd5);
                    OnlineLearningException.cast("合并文件校验失败");
                }
            } catch (IOException e) {
                e.printStackTrace();
                OnlineLearningException.cast("合并文件校验过程失败");
            }
            // 将合并后的文件上传到MinIO
            // 生成MinIO的存放路径 在/a/1asdfadfadfasdfa/下 与chunk同级
            String objectName = getChunkFolderPathByMd5(fileMd5, extension);
            addMediaFile2MinIO(bucket_videofiles, tempMergeFile.getAbsolutePath(), objectName);
            // 将文件信息入库
            uploadFileParamsDto.setFileSize(tempMergeFile.length());
            addMediaInfo2DB(companyId, uploadFileParamsDto, objectName, fileMd5, bucket_videofiles);
            return RestResponse.success(true);
        } finally {
            if (chunks != null) {
                for (File chunk : chunks) {
                    if (chunk.exists()) chunk.delete();
                }
            }
            if (tempMergeFile != null) {
                tempMergeFile.delete();
            }
        }
    }

    /**
     * 根据媒体id查询媒体文件
     *
     * @param mediaId
     * @return
     */
    @Override
    public MediaFiles getFileById(String mediaId) {
        MediaFiles mediaFile = mediaFilesMapper.selectById(mediaId);
        if (mediaFile == null) {
            OnlineLearningException.cast("文件不存在");
        }
        if (StringUtils.isEmpty(mediaFile.getUrl())) {
            OnlineLearningException.cast("文件还未处理请稍后预览");
        }
        return mediaFile;
    }

    // ========================= private methods =========================

    /**
     * @param fileMd5
     * @param chunkTotal
     */
    private File[] checkChunkStatus(String fileMd5, int chunkTotal) {
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        // 创建File数组
        File[] files = new File[chunkTotal];
        for (int i = 0; i < chunkTotal; i++) {
            String chunkPath = chunkFolderPath + i;
            File chunkFile = null;
            try {
                chunkFile = File.createTempFile("chuck", null);
            } catch (IOException e) {
                OnlineLearningException.cast("创建临时分片文件出错");
            }
            File file = downLoadFileFromMinIO(chunkPath, chunkFile, bucket_videofiles);
            files[i] = file;
        }
        return files;
    }

    private File downLoadFileFromMinIO(String chunkPath, File chunkFile, String bucket) {
        GetObjectArgs build = GetObjectArgs.builder()
                .bucket(bucket)
                .object(chunkPath)
                .build();

        try (InputStream stream = minioClient.getObject(build);
             FileOutputStream outputStream = new FileOutputStream(chunkFile)
        ) {
            IOUtils.copy(stream, outputStream);
            return chunkFile;
        } catch (Exception e) {
            OnlineLearningException.cast("下载分片异常");
            e.printStackTrace();
        }
        return null;
    }

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

    private String getChunkFolderPathByMd5(String fileMd5, String extension) {
        return fileMd5.charAt(0) +
                "/" +
                fileMd5.charAt(1) +
                "/" +
                fileMd5 +
                "/" +
                fileMd5 + extension;
    }

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

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
    public MediaFiles addMediaInfo2DB(Long companyId, UploadFileParamsDto uploadFileParamsDto, String objectName, String md5, String bucketName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(md5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            // 封装数据
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(md5);
            mediaFiles.setFileId(md5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucketName);
            mediaFiles.setFilePath(objectName);
            // 获取扩展名
            String extension = null;
            String filename = uploadFileParamsDto.getFilename();
            if (StringUtils.isNotBlank(filename) && filename.contains(".")) {
                extension = filename.substring(filename.indexOf("."));
            }
            String mimeType = getMimeTypeByExtension(extension);
            // 图片 mp4视频可以设置URL
            if (mimeType.contains("image") || mimeType.contains("mp4")) {
                mediaFiles.setUrl("/" + bucketName + "/" + objectName);
            }
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            // 插入文件列表
            mediaFilesMapper.insert(mediaFiles);

            // 对avi格式视频进行处理
            if (mimeType.equals("video/x-msvideo")) {
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtils.copyProperties(mediaFiles, mediaProcess);
                // 设置未处理状态
                mediaProcess.setStatus("1");
                mediaProcessMapper.insert(mediaProcess);
            }
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
            String contentType = getMimeTypeByExtension(objectName);
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

    private String getMimeTypeByExtension(String objectName) {
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
        return contentType;
    }

    /**
     * 上传大文件专用
     *
     * @param bucketName 桶名
     * @param filePath   本地文件路径
     * @param objectName 文件系统路径
     */
    private void addMediaFile2MinIO(String bucketName, String filePath, String objectName) {
        try {
            UploadObjectArgs build = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .filename(filePath)
                    .object(objectName)
                    .build();
            minioClient.uploadObject(build);
            log.debug("文件上传成功:{}", filePath);
        } catch (Exception e) {
            OnlineLearningException.cast("大文件上传到文件系统失败");
            e.printStackTrace();
        }
    }

    /**
     * 默认需要路径全生成
     *
     * @return
     */
    private String generateFileFolder() {
        return this.generateFileFolder(true, true, true);
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
