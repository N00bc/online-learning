package com.cyn.onlinelearning.content.service.impl;

import com.cyn.onlinelearning.base.exception.OnlineLearningException;
import com.cyn.onlinelearning.content.mapper.CourseBaseMapper;
import com.cyn.onlinelearning.content.mapper.TeachplanMapper;
import com.cyn.onlinelearning.content.mapper.TeachplanMediaMapper;
import com.cyn.onlinelearning.content.service.TeachPlanService;
import com.cyn.onlinelearning.model.dto.BindTeachplanMediaDto;
import com.cyn.onlinelearning.model.dto.SaveTeachplanDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import com.cyn.onlinelearning.model.po.CourseBase;
import com.cyn.onlinelearning.model.po.Teachplan;
import com.cyn.onlinelearning.model.po.TeachplanMedia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
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
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

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

    /**
     * 根据课程计划Id删除课程计划
     * <br>只有当课程是未提交时方可删除
     * <br>删除第一级别的章时要求章下边没有小节方可删除。
     * <br>删除第二级别的小节的同时需要将其它关联的视频信息也删除。
     *
     * @param teachPlanId
     */
    @Override
    public void removeTeachPlan(Long teachPlanId) {
        // 合法性判断
        if (teachPlanId == null) {
            OnlineLearningException.cast("参数异常");
        }
        Teachplan teachplan = teachplanMapper.selectById(teachPlanId);
        if (teachplan == null) {
            OnlineLearningException.cast("查无此计划");
        }
        CourseBase courseBase = courseBaseMapper.selectById(teachplan.getCourseId());
        String auditStatus = courseBase.getAuditStatus();
        // 只有当课程是未提交时方可删除
        if (!"202002".equals(auditStatus)) {
            OnlineLearningException.cast("删除失败，课程审核状态是未提交时方可删除。");
        }
        //  删除第一级别的章时要求章下边没有小节方可删除。
        //  删除第二级别的小节的同时需要将其它关联的视频信息也删除。
        if (Long.valueOf(0L).equals(teachplan.getParentid())) {
            // 删除第一级别章节
            Integer count = teachplanMapper.selectChildCount(teachPlanId);
            if (count > 0) {
                OnlineLearningException.cast("还有未删除的小节");
            } else {
                teachplanMapper.deleteById(teachPlanId);
            }
        } else {
            // 删除第二级别小节
            // TODO 需要删除媒体的数据

        }
    }

    /**
     * 移动课程计划x
     *
     * @param moveType    移动类型
     * @param teachplanId 需要移动的计划
     */
    @Override
    public void moveTeachPlan(String moveType, Long teachplanId) {
        // 根据教学计划id 获取同级别计划列表
        List<Teachplan> list = teachplanMapper.selectSameLevelList(teachplanId);
        // 如果只有一个元素则不需要额外操作
        if (list.size() == 1) {
            return;
        }
        // 目标:将当前对象的orderby与后一个对象的orderby交换
        if ("moveup".equals(moveType)) {
            // 向上移  当前的小 原来的大 需要降序排列
            list.sort((o1, o2) -> o2.getOrderby() - o1.getOrderby());
        } else {
            // 向下移  当前的大 原来的小 需要升序排列
            list.sort(Comparator.comparingInt(Teachplan::getOrderby));
        }
        // 排序
        Teachplan one = null;
        Teachplan two = null;
        for (Teachplan teachplan : list) {
            if (teachplanId.equals(teachplan.getId())) {
                one = teachplan;
                continue;
            }
            if (one != null) {
                two = teachplan;
                break;
            }
        }
        swapOrderBy(one, two);
    }

    /**
     * 绑定教学计划与媒资 1对多的关系， 多条媒资可以定义一条教学计划
     *
     * @param dto
     * @return
     */
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto dto) {
        Long teachplanId = dto.getTeachplanId();
        // 约束校验
        // 1.教学计划不存在 无法绑定
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            OnlineLearningException.cast("教学计划不存在,无法绑定");
        }
        // 2.只有二级目录才能绑定视频
        Integer grade = teachplan.getGrade();
        if (2 != grade) {
            OnlineLearningException.cast("只有二级目录才能绑定视频");
        }
        // 删除原来的绑定关系
        teachplanMediaMapper.deleteByTeachPlanId(teachplanId);
        // 添加新记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(dto.getFileName());
        teachplanMedia.setMediaId(dto.getMediaId());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMediaMapper.insert(teachplanMedia);
        return teachplanMedia;
    }

    public void swapOrderBy(Teachplan one, Teachplan two) {
        Integer orderBy = one.getOrderby();
        one.setOrderby(two.getOrderby());
        two.setOrderby(orderBy);
        teachplanMapper.updateById(one);
        teachplanMapper.updateById(two);
    }

    public int getOrderByLevel(Long courseId, Long parentId) {
        Integer value = teachplanMapper.selectOrderByLevel(courseId, parentId);
        return value == null ? 0 : value;
    }
}
