package io.charlie.galaxy.exception;

import lombok.Getter;
import io.charlie.galaxy.result.IResultCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 业务异常
 */
@Getter
public class BusinessException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public transient IResultCode resultCode;

    public BusinessException(IResultCode errorCode) {
        super(errorCode.getMessage());
        this.resultCode = errorCode;
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

}