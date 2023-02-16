package com.cyn.onlinelearning.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import com.cyn.onlinelearning.model.po.Teachplan;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    List<TeachplanDto> getTreeNodes(@Value("courseId") Long courseId);
}
