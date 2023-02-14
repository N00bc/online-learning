package com.cyn.onlinelearning.content.api;

import com.cyn.onlinelearning.content.service.impl.CourseCategoryServiceImpl;
import com.cyn.onlinelearning.model.dto.CourseCategoryTreeDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 21:56
 */
@Slf4j
@RestController
@Api(value = "课程分类相关接口", tags = "课程分类相关接口")
public class CourseCategoryController {
    @Autowired
    private CourseCategoryServiceImpl courseCategoryService;

    @ApiOperation("课程分类查询接口")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes();
    }

}
