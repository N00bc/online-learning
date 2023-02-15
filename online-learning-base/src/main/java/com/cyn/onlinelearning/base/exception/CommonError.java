package com.cyn.onlinelearning.base.exception;

/**
 * @author Godc
 * @description:
 * @date 2023/2/15 23:07
 */
public enum CommonError {
    UNKNOWN_ERROR("执行过程异常，请重试"),
    PARAMS_ERROR("非法参数"),
    OBJECT_ERROR(""),
    QUERY_NULL("查询结果为空"),
    QUEST_ERROR("请求参数为空");

    private String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    CommonError(String errMsg) {
        this.errMsg = errMsg;
    }
}
