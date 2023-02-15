package com.cyn.onlinelearning.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyn.onlinelearning.base.exception.OnlineLearningException;
import com.cyn.onlinelearning.base.model.PageParams;
import com.cyn.onlinelearning.base.model.PageResult;
import com.cyn.onlinelearning.content.mapper.CourseBaseMapper;
import com.cyn.onlinelearning.content.mapper.CourseCategoryMapper;
import com.cyn.onlinelearning.content.mapper.CourseMarketMapper;
import com.cyn.onlinelearning.content.service.CourseBaseInfoService;
import com.cyn.onlinelearning.model.dto.AddCourseDto;
import com.cyn.onlinelearning.model.dto.CourseBaseInfoDto;
import com.cyn.onlinelearning.model.dto.QueryCourseParamsDto;
import com.cyn.onlinelearning.model.po.CourseBase;
import com.cyn.onlinelearning.model.po.CourseCategory;
import com.cyn.onlinelearning.model.po.CourseMarket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 19:03
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 课程查询
     *
     * @param params               分页参数，query查询
     * @param queryCourseParamsDto 查询条件，请求体查询
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams params, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> wrapper = Wrappers.<CourseBase>lambdaQuery()
                .like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        // 分页查询 需要一个page对象，存放当前页码，记录数。
        Page<CourseBase> page = new Page<>(params.getPageNo(), params.getPageSize());
        page = courseBaseMapper.selectPage(page, wrapper);
        PageResult<CourseBase> result = new PageResult<CourseBase>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
        return result;
    }

    /**
     * @param companyId 公司ID TODO 需要在登录功能完成后获取公司ID
     * @param dto       请求参数
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 参数合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//            OnlineLearningException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            OnlineLearningException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            OnlineLearningException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            OnlineLearningException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            OnlineLearningException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            OnlineLearningException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//             OnlineLearningException.cast("收费规则为空");
//        } else
        if ("201001".equals(dto.getCharge()) &&
                (dto.getPrice() == null || dto.getPrice().floatValue() <= 0)) {
            OnlineLearningException.cast("收费价格异常");
        }
        // 向课程基本信息表插入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);
        // 设置公司ID
        courseBase.setCompanyId(companyId);
        // 设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        // 审核状态：未提交
        courseBase.setAuditStatus("202002");
        // 发布状态：未发布
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        // 向课营销表插入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        // 课程基本信息插入后会返回ID
        courseMarket.setId(courseBase.getId());
        int insert1 = courseMarketMapper.insert(courseMarket);
        if (insert <= 0 || insert1 <= 0) {
            // 抛出异常才可以被Spring TX接收
            OnlineLearningException.cast("插入信息异常");
        }
        return getCourseBaseInfo(courseBase.getId());
    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        if (courseBase == null) {
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }
}
