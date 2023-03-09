package com.cyn.onlinelearning.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Godc
 * @description:
 * @date 2023/3/4 22:05
 */
@Controller
public class FreemarkerController {
    @GetMapping("/hello")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        // 设置视图名称 templates 下的文件名后缀不写
        // 指定 test.ftl
        modelAndView.setViewName("test");
        modelAndView.addObject("name", "小明");
        return modelAndView;
    }
}
