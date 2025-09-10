package io.charlie.galaxy.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import io.charlie.galaxy.result.Result;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public <T> Result<T> handleBizException(BusinessException e) {
        if (e.getResultCode() != null) {
            return Result.failure(e.getResultCode());
        }
        log.error("业务异常：{}", e.getMessage());
        e.printStackTrace();
        return Result.failure(e.getMessage());
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public <T> Result<T> processException(NoHandlerFoundException e) {
        log.error("请求地址不存在：{}", e.getMessage());
        e.printStackTrace();
        return Result.failure(ResultCode.NOT_FOUND, e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public <T> Result<T> processException(RuntimeException e) {
        log.error("系统异常：{}", e.getMessage());
        e.printStackTrace();
        return Result.failure(ResultCode.SYSTEM_ERROR, ResultCode.SYSTEM_ERROR.getMessage());
    }

    /**
     * 处理参数类型转换失败（如 @PathVariable 或 @RequestParam 类型不匹配）
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public <T> Result<T> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String errorMsg = String.format("参数 '%s' 类型错误，应为 %s 类型",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.error("参数类型错误：{}", errorMsg);
        e.printStackTrace();
        return Result.failure(ResultCode.PARAM_ERROR, errorMsg);
    }

    /**
     * 处理 @RequestParam 参数校验失败异常（Spring 6+）
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public Result<?> handleHandlerMethodValidationException(ValidationException e) {
        String errorMsg = e.getMessage();
        log.error("参数校验异常：{}", errorMsg);
        e.printStackTrace();
        return Result.failure(ResultCode.PARAM_ERROR, errorMsg);
    }

    /**
     * 处理 @Validated 方法参数校验异常（兼容旧版）
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .orElse("参数校验失败");
        log.error("参数校验异常：{}", errorMsg);
        e.printStackTrace();
        return Result.failure(ResultCode.PARAM_ERROR, errorMsg);
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        // 检查是否是SSE请求
        if (isSseRequest(request)) {
            log.error("SSE请求异常: {}", e.getMessage());
            // 对于SSE请求，返回SseEmitter错误
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(e);
            e.printStackTrace();
            return emitter;
        }

        // 普通请求返回Result对象
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.failure("系统异常: " + e.getMessage());
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String uri = request.getRequestURI();
        return (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
                || uri.contains("/api/sse/");
    }

    // 专门处理SSE相关的异常
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        if (isSseRequest(request)) {
            log.info("SSE连接超时: {}", e.getMessage());
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            log.warn("异步请求超时: {}", e.getMessage());
        }
    }
}
