package com.cyn.onlinelearning.model.dto;

import com.cyn.onlinelearning.model.po.CourseCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/13 22:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CourseCategoryTreeDto extends CourseCategory {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
