package com.cyn.onlinelearning.content.service.impl;

import com.cyn.onlinelearning.content.mapper.TeachplanMapper;
import com.cyn.onlinelearning.content.service.TeachPlanService;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/16 15:40
 */
@Service
@Slf4j
@Transactional
public class TeachPlanServiceImpl implements TeachPlanService {
    @Autowired
    private TeachplanMapper teachplanMapper;

    /**
     * 课程计划树型结构查询
     *
     * @param courseId 课程id
     * @return
     */
    @Override
    public List<TeachplanDto> findTeachPlanTree(Long courseId) {
        List<TeachplanDto> result = teachplanMapper.getTreeNodes(courseId);
        return result;
    }
}
