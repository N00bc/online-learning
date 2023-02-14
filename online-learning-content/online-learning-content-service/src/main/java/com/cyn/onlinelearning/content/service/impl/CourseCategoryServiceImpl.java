package com.cyn.onlinelearning.content.service.impl;

import com.cyn.onlinelearning.content.mapper.CourseCategoryMapper;
import com.cyn.onlinelearning.content.service.CourseCategoryService;
import com.cyn.onlinelearning.model.dto.CourseCategoryTreeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 23:09
 */
@Service
@Slf4j
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 查询课程分类 需要返回树状结构
     *
     * @return
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        // 查询所有分类
        List<CourseCategoryTreeDto> dtos = courseCategoryMapper.selectAll();
        for (CourseCategoryTreeDto dto : dtos) {
            if ("1".equals(dto.getId())) {
                dto.setChildrenTreeNodes(getChildrenNode(dto.getId(), dtos));
                return dto.getChildrenTreeNodes();
            }
        }
        return Collections.EMPTY_LIST;
    }

    private List<CourseCategoryTreeDto> getChildrenNode(String id, List<CourseCategoryTreeDto> dtos) {
        List<CourseCategoryTreeDto> childList = new ArrayList<>();
        for (CourseCategoryTreeDto dto : dtos) {
            if (id != null && id.equals(dto.getParentid())) {
                dto.setChildrenTreeNodes(getChildrenNode(dto.getId(), dtos));
                childList.add(dto);
            }
        }
        return childList;
    }
}
