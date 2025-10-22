package io.charlie.web.gateway.exception;

import io.charlie.web.gateway.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;

@RestControllerAdvice
@Slf4j
public class GatewayExceptionHandler {

    /**
     * 处理服务未找到异常 (503)
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(NotFoundException.class)
    public <T> Result<T> handleNotFoundException(NotFoundException e) {
        log.error("服务实例未找到: ", e);
        return Result.failure("服务暂时不可用，请稍后重试");
    }

    /**
     * 处理服务不可用异常 (503)
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public <T> Result<T> handleServiceUnavailable(ServiceUnavailableException e) {
        log.error("服务不可用: ", e);
        return Result.failure("服务暂时不可用，请稍后重试");
    }

    /**
     * 处理连接超时、服务不可用等情况
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ConnectException.class, TimeoutException.class})
    public <T> Result<T> handleConnectException(Exception e) {
        log.error("服务连接失败: ", e);
        return Result.failure("后端服务不可用，请稍后重试");
    }

    /**
     * 处理其他异常 (500)
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public <T> Result<T> handleException(Exception e) {
        log.error("网关内部错误: ", e);
        return Result.failure("服务器内部错误，请稍后重试");
    }
}