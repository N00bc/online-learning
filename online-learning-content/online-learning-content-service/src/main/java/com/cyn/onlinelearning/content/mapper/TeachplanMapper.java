package com.cyn.onlinelearning.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import com.cyn.onlinelearning.model.po.Teachplan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> getTreeNodes(@Param("courseId") Long courseId);

    /**
     * 找到同级课程的orderby字段
     * @param courseId
     * @param parentId
     * @return
     */
    Integer selectOrderByLevel(@Param("courseId") Long courseId, @Param("parentId") Long parentId);
}
