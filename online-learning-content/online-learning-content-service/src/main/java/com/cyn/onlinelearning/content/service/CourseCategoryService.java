package com.cyn.onlinelearning.content.service;

import com.cyn.onlinelearning.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 23:08
 */
public interface CourseCategoryService {
    /**
     * 查询课程分类 需要返回树状结构
     * @return
     */
    List<CourseCategoryTreeDto> queryTreeNodes();
}
