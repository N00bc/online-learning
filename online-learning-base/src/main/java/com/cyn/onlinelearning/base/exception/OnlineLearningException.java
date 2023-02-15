package com.cyn.onlinelearning.base.exception;

/**
 * @author Godc
 * @description:
 * @date 2023/2/15 21:52
 */
public class OnlineLearningException extends RuntimeException {

    private String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    public OnlineLearningException() {
        super();
    }

    public OnlineLearningException(String message) {
        super(message);
        this.errMsg = message;
    }

    public static void cast(String errMsg) {
        throw new OnlineLearningException(errMsg);
    }

    public static void cast(CommonError commonError) {
        throw new OnlineLearningException(commonError.getErrMsg());
    }
}
