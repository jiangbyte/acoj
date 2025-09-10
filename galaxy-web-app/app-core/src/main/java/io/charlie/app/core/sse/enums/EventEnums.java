package io.charlie.app.core.sse.enums;

import io.charlie.galaxy.enums.ILabelEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 09/09/2025
 * @description 事件枚举
 */
@AllArgsConstructor
@Getter
public enum EventEnums implements ILabelEnum<String> {
    // 心跳
    HEARTBEAT("heartbeat", "心跳"),
    // 初始化欢迎数据
    INITIAL_DATA("initial_data", "初始化数据"),
    // 响应心跳
    HEARTBEAT_ACK("heartbeat_ack", "响应心跳"),
    // 广播
    BROADCAST("broadcast", "广播"),
    // 推送消息
    MESSAGE("message", "推送消息"),
    // 关闭连接
    CONNECTION_CLOSED("connection_closed", "连接关闭"),
    // 错误
    ERROR("error", "错误")
    ;

    private final String value;
    private final String label;
}
