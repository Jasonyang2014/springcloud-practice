package com.auyeung.seata.config;

import com.auyeung.seata.vo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionConfig {


    @ExceptionHandler(value = {Exception.class})
    public Object globalExceptionHandler(Exception ex) {
        return Result.fail(ex.getMessage());
    }
}
