package com.cyn.onlinelearning.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyn.onlinelearning.model.po.TeachplanMedia;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMediaMapper extends BaseMapper<TeachplanMedia> {

    /**
     * 根据教学计划删除 关系
     * @param teachplanId
     */
    void deleteByTeachPlanId(@Param("teachplanId") Long teachplanId);
}
