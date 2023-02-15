package com.cyn.onlinelearning.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @author Godc
 * @description:
 * @date 2023/2/15 23:15
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 捕获主动抛出的异常
    @ExceptionHandler(OnlineLearningException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)// 返回状态码注解
    public RestErrorResponse doOnlineLearningException(OnlineLearningException exception) {
        String errMsg = exception.getErrMsg();
        // 将异常信息写入日志
        log.error("捕获异常信息:{}", errMsg);
        exception.printStackTrace();
        return new RestErrorResponse(errMsg);
    }

    // 捕获未知的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)// 返回状态码注解
    public RestErrorResponse doException(Exception exception) {
        String errMsg = exception.getMessage();
        // 将异常信息写入日志
        log.error("捕获异常信息:{}", errMsg);
        exception.printStackTrace();
        // 若发生系统内部异常则返回友好提示
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)// 返回状态码注解
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        // 获取错误集合
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        // 将错误信息收集
        StringBuffer errorMsg = new StringBuffer();
        fieldErrors.forEach(error -> errorMsg.append(error.getDefaultMessage()).append(","));
        log.error("捕获异常信息:{}", errorMsg);
        exception.printStackTrace();
        // 若发生系统内部异常则返回友好提示
        return new RestErrorResponse(errorMsg.toString());
    }
}
