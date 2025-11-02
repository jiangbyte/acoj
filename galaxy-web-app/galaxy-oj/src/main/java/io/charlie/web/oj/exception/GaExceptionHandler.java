package io.charlie.web.oj.exception;

import cn.dev33.satoken.exception.NotLoginException;
import io.charlie.galaxy.result.IResultCode;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GaExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public <T> Result<T> handleException(NotLoginException e) {
        return Result.failure(ResultCode.UNAUTHORIZED);
    }
}