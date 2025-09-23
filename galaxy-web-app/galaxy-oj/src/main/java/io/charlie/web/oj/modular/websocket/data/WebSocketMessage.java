package io.charlie.web.oj.modular.websocket.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.charlie.galaxy.config.timestamp.DateToTimestampSerializer;
import io.charlie.galaxy.config.timestamp.TimestampToDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * WebSocket消息通用响应格式
 */
@Data
public class WebSocketMessage<T> {
    private String type;          // 消息类型
    private T data;               // 消息数据

    @JsonSerialize(using = DateToTimestampSerializer.class)
    @JsonDeserialize(using = TimestampToDateDeserializer.class)
    private Date timestamp; // 时间戳
    private boolean success;      // 是否成功
    private String message;       // 附加消息

    public WebSocketMessage() {
        this.timestamp = new Date();
    }

    public WebSocketMessage(String type, T data) {
        this.type = type;
        this.data = data;
        this.success = true;
        this.timestamp = new Date();
    }

    public WebSocketMessage(String type, T data, boolean success) {
        this.type = type;
        this.data = data;
        this.success = success;
        this.timestamp = new Date();
    }

    public WebSocketMessage(String type, T data, boolean success, String message) {
        this.type = type;
        this.data = data;
        this.success = success;
        this.message = message;
        this.timestamp = new Date();
    }

    public static <T> WebSocketMessage<T> success(String type, T data) {
        return new WebSocketMessage<>(type, data, true, "操作成功");
    }

    public static <T> WebSocketMessage<T> error(String type, String message) {
        return new WebSocketMessage<>(type, null, false, message);
    }

    public static <T> WebSocketMessage<T> of(String type, T data, boolean success, String message) {
        return new WebSocketMessage<>(type, data, success, message);
    }
}