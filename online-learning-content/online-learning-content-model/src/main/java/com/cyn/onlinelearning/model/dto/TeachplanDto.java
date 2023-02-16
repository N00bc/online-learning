package com.cyn.onlinelearning.model.dto;

import com.cyn.onlinelearning.model.po.Teachplan;
import com.cyn.onlinelearning.model.po.TeachplanMedia;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/16 14:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeachplanDto extends Teachplan {
    // 关联的媒资信息
    TeachplanMedia teachplanMedia;
    // 子目录
    List<TeachplanDto> teachPlanTreeNodes;
}
