package com.cyn.onlinelearning.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.content.mapper.CourseBaseMapper;
import com.cyn.onlinelearning.content.service.CourseBaseInfoService;
import com.cyn.onlinelearning.model.dto.QueryCourseParamsDto;
import com.cyn.onlinelearning.model.po.CourseBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 19:03
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 课程查询
     *
     * @param params               分页参数，query查询
     * @param queryCourseParamsDto 查询条件，请求体查询
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> wrapper = Wrappers.<CourseBase>lambdaQuery()
                .like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        // 分页查询 需要一个page对象，存放当前页码，记录数。
        Page<CourseBase> page = new Page<>(params.getPageNo(), params.getPageSize());
        page = courseBaseMapper.selectPage(page, wrapper);
        PageResult<CourseBase> result = new PageResult<CourseBase>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
        return result;
    }
}
