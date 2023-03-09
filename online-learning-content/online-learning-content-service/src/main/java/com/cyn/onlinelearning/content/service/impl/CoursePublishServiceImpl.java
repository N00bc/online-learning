package com.cyn.onlinelearning.content.service.impl;


import com.cyn.onlinelearning.content.service.CourseBaseInfoService;
import com.cyn.onlinelearning.content.service.CoursePublishService;
import com.cyn.onlinelearning.content.service.TeachPlanService;
import com.cyn.onlinelearning.model.dto.CourseBaseInfoDto;
import com.cyn.onlinelearning.model.dto.CoursePreviewDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachPlanService teachPlanService;

    /**
     * 获取课程预览信息
     *
     * @param courseId 课程ID
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        /**
         *     CourseBaseInfoDto courseBase;
         *     List<TeachplanDto> teachplans;
         */
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachplanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachPlanTree);
        return coursePreviewDto;
    }
}