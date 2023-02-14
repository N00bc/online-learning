package com.cyn.onlinelearning.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyn.onlinelearning.model.dto.CourseCategoryTreeDto;
import com.cyn.onlinelearning.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author godc
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    List<CourseCategoryTreeDto> selectAll();

}
