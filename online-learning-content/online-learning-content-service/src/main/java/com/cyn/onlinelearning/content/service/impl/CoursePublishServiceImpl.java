package com.cyn.onlinelearning.content.service.impl;


import com.cyn.onlinelearning.base.exception.OnlineLearningException;
import com.cyn.onlinelearning.content.mapper.CourseBaseMapper;
import com.cyn.onlinelearning.content.mapper.CourseMarketMapper;
import com.cyn.onlinelearning.content.mapper.CoursePublishPreMapper;
import com.cyn.onlinelearning.content.service.CourseBaseInfoService;
import com.cyn.onlinelearning.content.service.CoursePublishService;
import com.cyn.onlinelearning.content.service.TeachPlanService;
import com.cyn.onlinelearning.model.dto.CourseBaseInfoDto;
import com.cyn.onlinelearning.model.dto.CoursePreviewDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import com.cyn.onlinelearning.model.po.CourseBase;
import com.cyn.onlinelearning.model.po.CourseMarket;
import com.cyn.onlinelearning.model.po.CoursePublishPre;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;
    @Autowired
    private TeachPlanService teachPlanService;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

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
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachplanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlanTree);
        return coursePreviewDto;
    }

    /**
     * 将查询到的课程基本信息、营销信息、计划等信息插入到课程预发布表
     *
     * @param companyId
     * @param courseId
     */
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 课程信息不能为空
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            OnlineLearningException.cast("课程信息为空");
        }
        //
        String status = courseBaseInfo.getStatus();
        if (status.equals("202003")) {
            OnlineLearningException.cast("课程已提交");
        }
        // 课程的图片、计划没有填写也不允许提交
        if (StringUtils.isBlank(courseBaseInfo.getPic())) {
            OnlineLearningException.cast("需要上传课程图片");
        }
        List<TeachplanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        if (teachPlanTree.isEmpty()) {
            OnlineLearningException.cast("需要上传课程计划");
        }
        // 将courseBaseInfo,teachPlan,courseMarket 插入到 course_publish_pre
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketString = null;
        String teachPlanTreeString = null;
        try {
            courseMarketString = new ObjectMapper().writeValueAsString(courseMarket);
            teachPlanTreeString = new ObjectMapper().writeValueAsString(teachPlanTree);
        } catch (JsonProcessingException e) {
            OnlineLearningException.cast("转换JSON异常");
            log.error("com.cyn.onlinelearning.content.service.impl.CoursePublishServiceImpl.commitAudit-转换JSON异常:{}", e.getMessage());
        }
        coursePublishPre.setMarket(courseMarketString);
        coursePublishPre.setTeachplan(teachPlanTreeString);
        // 修改状态为已提交
        coursePublishPre.setStatus("202003");
        // 更新courseBaseInfo的审核状态 为已提交
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            // 插入数据
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            // 如果有数据则更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        // 更新课程基本信息表 status字段
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
}