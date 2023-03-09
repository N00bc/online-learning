package com.cyn.onlinelearning.content.service;


import com.cyn.onlinelearning.model.dto.CoursePreviewDto;

public interface CoursePublishService {

    /**
     * 获取课程预览信息
     * @param courseId 课程ID
     * @return
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);
}