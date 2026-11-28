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

    @ExceptionHandler(NotLoginException.class)
    public Object handleNotLoginException(NotLoginException e,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        return respondJsonOrSseSilent(request, response, HttpStatus.UNAUTHORIZED,
                Result.failure(ResultCode.UNAUTHORIZED));
    }

    @ExceptionHandler(NotPermissionException.class)
    public Object handleNotPermissionException(NotPermissionException e,
                                               HttpServletRequest request,
                                               HttpServletResponse response) {
        return respondJsonOrSseSilent(request, response, HttpStatus.UNAUTHORIZED,
                Result.failure(ResultCode.UNAUTHORIZED));
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBizException(BusinessException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        Result<?> body;
        if (e.getResultCode() != null) {
            body = Result.failure(e.getResultCode());
        } else {
            log.error("业务异常：{}", e.getMessage());
            e.printStackTrace();
            body = Result.failure(e.getMessage());
        }
        return respondJsonOrSseSilent(request, response, HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Object processException(NoHandlerFoundException e,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        log.error("请求地址不存在：{}", e.getMessage());
        e.printStackTrace();
        return respondJsonOrSseSilent(request, response, HttpStatus.NOT_FOUND,
                Result.failure(ResultCode.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object processNoResourceFoundException(NoResourceFoundException e,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        log.error("请求地址不存在：{}", e.getMessage());
        return respondJsonOrSseSilent(request, response, HttpStatus.NOT_FOUND,
                Result.failure(ResultCode.NOT_FOUND, e.getMessage()));
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

    /**
     * 未分类 RuntimeException：断开/SSE 静默，普通请求再写 JSON 500。
     */
    @ExceptionHandler(RuntimeException.class)
    public Object processException(RuntimeException e,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        // 1. 断开类异常静默结束，避免强制 500 + 写 Result
        if (isClientDisconnected(e) || (isSseRequest(request) && isBrokenPipe(e))) {
            log.debug("客户端已断开(Runtime): uri={} msg={}", request.getRequestURI(), e.getMessage());
            return null;
        }
        // 2. SSE 其它运行时异常：只打日志，不写 JSON（produces 已是 event-stream）
        if (isSseRequest(request)) {
            log.error("SSE系统异常: uri={} msg={}", request.getRequestURI(), e.getMessage());
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            return null;
        }
        // 3. 普通请求返回统一错误体
        log.error("系统异常：{}", e.getMessage());
        e.printStackTrace();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Result.failure(ResultCode.SYSTEM_ERROR, ResultCode.SYSTEM_ERROR.getMessage());
    }

    /**
     * 处理参数类型转换失败（如 @PathVariable 或 @RequestParam 类型不匹配）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Object handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String errorMsg = String.format("参数 '%s' 类型错误，应为 %s 类型",
                e.getName(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        log.error("参数类型错误：{}", errorMsg);
        e.printStackTrace();
        return respondJsonOrSseSilent(request, response, HttpStatus.BAD_REQUEST,
                Result.failure(ResultCode.PARAM_ERROR, errorMsg));
    }

    /**
     * 处理 @RequestParam 参数校验失败异常（Spring 6+）
     */
    @ExceptionHandler(ValidationException.class)
    public Object handleHandlerMethodValidationException(ValidationException e,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        String errorMsg = e.getMessage();
        log.error("参数校验异常：{}", errorMsg);
        e.printStackTrace();
        return respondJsonOrSseSilent(request, response, HttpStatus.BAD_REQUEST,
                Result.failure(ResultCode.PARAM_ERROR, errorMsg));
    }

    /**
     * 处理 @Validated 方法参数校验异常（兼容旧版）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .orElse("参数校验失败");
        log.error("参数校验异常：{}", errorMsg);
        e.printStackTrace();
        return respondJsonOrSseSilent(request, response, HttpStatus.BAD_REQUEST,
                Result.failure(ResultCode.PARAM_ERROR, errorMsg));
    }

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        // 1. 客户端断开：静默结束
        if (isClientDisconnected(e) || isBrokenPipe(e)) {
            log.debug("客户端已断开: uri={} msg={}", request.getRequestURI(), e.getMessage());
            return null;
        }
        // 2. SSE 其它异常：只打日志，禁止再创建 SseEmitter / 写 Result
        if (isSseRequest(request)) {
            log.error("SSE请求异常: uri={} msg={}", request.getRequestURI(), e.getMessage());
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            return null;
        }
        // 3. 普通请求返回 Result
        log.error("系统异常: {}", e.getMessage(), e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Result.failure("系统异常: " + e.getMessage());
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleAsyncRequestTimeoutException(AsyncRequestTimeoutException e,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        if (isSseRequest(request)) {
            log.info("SSE连接超时: {}", e.getMessage());
            if (!response.isCommitted()) {
                response.setStatus(HttpStatus.NO_CONTENT.value());
            }
        } else {
            log.warn("异步请求超时: {}", e.getMessage());
        }
    }

    @ExceptionHandler(SubmitLimitException.class)
    public Object handleSubmitLimitException(SubmitLimitException e,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        log.info("提交限流: {}", e.getMessage());
        // 与历史行为一致：业务失败体 + HTTP 200
        return respondJsonOrSseSilent(request, response, HttpStatus.OK,
                Result.failure(e.getMessage()));
    }

    /**
     * 普通请求写 JSON；SSE 只设状态码（未提交时），避免 event-stream 上再写 Result。
     */
    private Object respondJsonOrSseSilent(HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpStatus status,
                                          Result<?> body) {
        // 1. SSE：禁止返回 Result，防止 Content-Type 冲突引发二次异常
        if (isSseRequest(request)) {
            if (!response.isCommitted()) {
                response.setStatus(status.value());
            }
            return null;
        }
        // 2. 普通请求：设置状态码并返回统一错误体
        response.setStatus(status.value());
        return body;
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
