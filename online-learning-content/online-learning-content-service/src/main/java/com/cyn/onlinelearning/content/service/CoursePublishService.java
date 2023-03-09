package com.cyn.onlinelearning.content.service;


import com.cyn.onlinelearning.model.dto.CoursePreviewDto;

public interface CoursePublishService {

    /**
     * 获取课程预览信息
     * @param courseId 课程ID
     * @return
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 将查询到的课程基本信息、营销信息、计划等信息插入到课程预发布表
     * @param companyId
     * @param courseId
     */
    void commitAudit(Long companyId,Long courseId);
}