package com.cyn.onlinelearning.content.api;

import com.cyn.onlinelearning.content.service.TeachPlanService;
import com.cyn.onlinelearning.model.dto.SaveTeachplanDto;
import com.cyn.onlinelearning.model.dto.TeachplanDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/16 14:23
 */
@RestController
@Api(value = "课程计划管理相关接口", tags = "课程计划管理相关接口")
@Slf4j
public class TeachPlanController {
    @Autowired
    private TeachPlanService teachPlanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachPlanService.findTeachPlanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachPlanService.saveTeachPlan(teachplan);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void removeTeachPlan(@PathVariable Long teachplanId) {
        teachPlanService.removeTeachPlan(teachplanId);
    }

    @ApiOperation(value = "移动课程计划")
    @PostMapping("teachplan/{moveType}/{teachplanId}")
    public void moveTeachPlan(@PathVariable String moveType, @PathVariable Long teachplanId) {
        teachPlanService.moveTeachPlan(moveType, teachplanId);
    }
}
