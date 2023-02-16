package com.cyn.onlinelearning.content.service;

import com.cyn.onlinelearning.model.dto.SaveTeachplanDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;

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
}
