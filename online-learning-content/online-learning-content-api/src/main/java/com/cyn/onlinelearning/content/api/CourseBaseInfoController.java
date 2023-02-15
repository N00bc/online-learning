package com.cyn.onlinelearning.content.api;

import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.content.service.CourseBaseInfoService;
import com.cyn.onlinelearning.model.dto.AddCourseDto;
import com.cyn.onlinelearning.model.dto.CourseBaseInfoDto;
import com.cyn.onlinelearning.model.dto.QueryCourseParamsDto;
import com.cyn.onlinelearning.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @date 2022/10/7 16:22
 */
@Api(value = "课程管理接口", tags = "课程管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams params, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(params, queryCourseParamsDto);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody AddCourseDto dto) {
        Long companyId = 11L;
        return courseBaseInfoService.createCourseBase(companyId, dto);

    }

}
