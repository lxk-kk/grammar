package com.compiler.grammar.exception;

import com.compiler.grammar.entity.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 10652
 */
@RestControllerAdvice
public class GlobalHandler {
    @ExceptionHandler(Exception.class)
    public Result handlerException(Exception e) {
        return new Result(0, e.getMessage());
    }
}
