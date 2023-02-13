package com.cyn.onlinelearning.content.service;

import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.model.dto.QueryCourseParamsDto;
import com.cyn.onlinelearning.model.po.CourseBase;

/**
 * @author Godc
 * @description: 课程管理Service
 * @date 2023/2/13 19:00
 */
public interface CourseBaseInfoService {

    /**
     * 课程查询
     * @param params 分页参数，query查询
     * @param queryCourseParamsDto 查询条件，请求体查询
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto);
}
