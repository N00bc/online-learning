package com.cyn.onlinelearning.content.service.impl;

import com.cyn.onlinelearning.content.mapper.TeachplanMapper;
import com.cyn.onlinelearning.content.service.TeachPlanService;
import com.cyn.onlinelearning.model.dto.SaveTeachplanDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import com.cyn.onlinelearning.model.po.Teachplan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /**
     * 此接口用于 保存、修改课程计划
     *
     * @param dto
     */
    @Override
    public void saveTeachPlan(SaveTeachplanDto dto) {
        Teachplan teachplan = teachplanMapper.selectById(dto.getId());
        if (teachplan == null) {
            // 如果为空需要插入数据
            teachplan = new Teachplan();
            BeanUtils.copyProperties(dto, teachplan);
            // 需要手动更新order by字段  修改时间
            int level = getOrderByLevel(dto.getCourseId(), dto.getParentid());
            teachplan.setOrderby(level + 1);
            teachplan.setChangeDate(LocalDateTime.now());
            teachplanMapper.insert(teachplan);
        } else {
            // 已存在需要更新数据
            BeanUtils.copyProperties(dto, teachplan);
            teachplan.setChangeDate(LocalDateTime.now());
            teachplanMapper.updateById(teachplan);
        }
    }

    public int getOrderByLevel(Long courseId, Long parentId) {
        Integer value = teachplanMapper.selectOrderByLevel(courseId, parentId);
        return value;
    }
}
