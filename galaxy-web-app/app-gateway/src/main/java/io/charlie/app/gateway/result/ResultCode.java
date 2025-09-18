package io.charlie.app.gateway.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author charlie-zhang-code
 * @version v1.0
 * @date 2025/4/13
 * @description 结果码枚举
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ResultCode implements IResultCode, Serializable {
    // 2xx (成功状态码)
    SUCCESS("200", "请求成功"),

    // 4xx (客户端错误状态码)
    PARAM_ERROR("400", "请求错误"),
    UNAUTHORIZED("401", "无权限访问"),
    LOGIN_EXPIRED("401", "登录过期"),
    NOT_FOUND("404", "找不到资源"),
    USERNAME_EXISTS("409", "用户名已存在"),
    USERNAME_NOT_EXISTS("410", "用户名不存在"),
    USERNAME_EXCEPTION("411", "用户异常"),
    PASSWORD_ERROR("412", "密码错误"),
    REGISTER_ERROR("413", "注册失败"),
    LOGIN_ERROR("414", "登录失败"),
    PARAM_NOT_NULL("420", "参数不能为空"),
    PARAM_PRESENCE("421", "参数存在"),
    PASSWORD_IS_TOO_SHORT("422", "密码过短"),

    QUEST_ERROR("430", "请求错误"),
    // 5xx (服务器错误状态码)
    SYSTEM_ERROR("500", "内部服务器错误"),
    GATEWAY_ERROR("502", "网关异常"),
    RPC_ERROR("503", "服务异常"),

    // 6xx (自定义状态码)
    BLOCK_ERROR("600", "状态锁定"),
    STATUS_ERROR("601", "状态异常"),
    NULL_ERROR("602", "空指针异常"),
    CLIENT_NOT_EXIST("603", "客户端不存在"),
    USER_NOT_EXIST("604", "用户不存在"),
    USER_BLOCKED("605", "用户被锁定"),
    USER_DISABLED("606", "用户被禁用"),
    ;

    private String code;
    private String message;

    private static final long serialVersionUID = 1L;
}
