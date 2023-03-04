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

    /**
     * 根据 teachplanId 和 mediaId 删除数据
     * @param teachplanId
     * @param mediaId
     */
    void deleteByTeachPlanIdAndMediaId(@Param("teachplanId") Long teachplanId, @Param("mediaId") String mediaId);
}
