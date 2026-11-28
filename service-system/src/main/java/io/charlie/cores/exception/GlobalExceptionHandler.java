package io.charlie.cores.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import io.charlie.cores.result.Result;
import io.charlie.cores.result.ResultCode;
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
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

/**
 * 全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotLoginException.class)
    public <T> Result<T> handleNotLoginException(NotLoginException e) {
        return Result.failure(ResultCode.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NotPermissionException.class)
    public <T> Result<T> handleNotPermissionException(NotPermissionException e) {
        return Result.failure(ResultCode.UNAUTHORIZED);
    }

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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public <T> Result<T> processNoResourceFoundException(NoResourceFoundException e) {
        log.error("请求地址不存在：{}", e.getMessage());
        return Result.failure(ResultCode.NOT_FOUND, e.getMessage());
    }

    /**
     * 客户端已断开（SSE Broken pipe 等）：不再写响应体，避免二次异常。
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException e, HttpServletRequest request) {
        // 1. SSE / 流式场景下客户端断开属于常态，仅记 debug
        // 2. 不返回 Result / SseEmitter，否则 Content-Type 已是 text/event-stream 时会再抛转换异常
        if (isSseRequest(request) || isClientDisconnected(e)) {
            log.debug("客户端已断开: uri={} msg={}", request.getRequestURI(), e.getMessage());
            return;
        }
        log.warn("异步请求不可用: {}", e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Object processException(RuntimeException e, HttpServletRequest request) {
        // 1. 断开类异常交给静默处理，避免 RuntimeException 处理器抢先返回 Result
        if (isClientDisconnected(e) || (isSseRequest(request) && isBrokenPipe(e))) {
            log.debug("客户端已断开(Runtime): uri={} msg={}", request.getRequestURI(), e.getMessage());
            return null;
        }
        log.error("系统异常：{}", e.getMessage());
        e.printStackTrace();
        if (isSseRequest(request)) {
            // SSE 已开始推流时不要再写 JSON Result
            return null;
        }
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
        // 1. 客户端断开：静默结束
        if (isClientDisconnected(e) || isBrokenPipe(e)) {
            log.debug("客户端已断开: uri={} msg={}", request.getRequestURI(), e.getMessage());
            return null;
        }
        // 2. SSE 其它异常：只打日志，禁止再创建 SseEmitter / 写 Result
        if (isSseRequest(request)) {
            log.error("SSE请求异常: uri={} msg={}", request.getRequestURI(), e.getMessage());
            return null;
        }
        // 3. 普通请求返回 Result
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.failure("系统异常: " + e.getMessage());
    }

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

    @ExceptionHandler(SubmitLimitException.class)
    public Result<?> handleSubmitLimitException(SubmitLimitException e) {
        log.info("提交限流: {}", e.getMessage());
        return Result.failure(e.getMessage());
    }

    /**
     * 是否为 SSE / 流式对话请求。
     */
    private boolean isSseRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();
        String uri = request.getRequestURI();
        return (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
                || (contentType != null && contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE))
                || (uri != null && (uri.contains("/chat/stream") || uri.contains("/api/sse/")));
    }

    /**
     * 判断异常链是否表示客户端已断开。
     */
    private boolean isClientDisconnected(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            if (cur instanceof AsyncRequestNotUsableException) {
                return true;
            }
            String name = cur.getClass().getName();
            // Tomcat ClientAbortException；Undertow 多为 IOException Broken pipe
            if (name.endsWith("ClientAbortException")) {
                return true;
            }
            if (isBrokenPipe(cur)) {
                return true;
            }
            String msg = cur.getMessage();
            if (msg != null && msg.toLowerCase().contains("disconnected client")) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private boolean isBrokenPipe(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            if (cur instanceof IOException) {
                String msg = cur.getMessage();
                if (msg != null) {
                    String lower = msg.toLowerCase();
                    if (lower.contains("broken pipe")
                            || lower.contains("connection reset")
                            || lower.contains("connection aborted")) {
                        return true;
                    }
                }
            }
            cur = cur.getCause();
        }
        return false;
    }
}
