package com.cyn.onlinelearning.content.service;

import com.cyn.onlinelearning.model.dto.BindTeachplanMediaDto;
import com.cyn.onlinelearning.model.dto.SaveTeachplanDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import com.cyn.onlinelearning.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/16 15:39
 */
public interface TeachPlanService {

    /**
     * 课程计划树型结构查询
     * @param courseId 课程id
     * @return
     */
    List<TeachplanDto> findTeachPlanTree(Long courseId);

    /**
     * 此接口用于 保存、修改课程计划
     * @param teachplan
     */
    void saveTeachPlan(SaveTeachplanDto teachplan);

    /**
     * 根据课程计划Id删除课程计划
     * @param teachPlanId
     */
    void removeTeachPlan(Long teachPlanId);

    /**
     * 移动课程计划
     * @param moveType 移动类型
     * @param teachplanId 需要移动的计划
     */
    void moveTeachPlan(String moveType, Long teachplanId);

    /**
     * 绑定教学计划与媒资 1对多的关系， 多条媒资可以定义一条教学计划
     * @param dto
     */
    TeachplanMedia associationMedia(BindTeachplanMediaDto dto);
}
