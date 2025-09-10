package io.charlie.app.core.sse.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SseEvent<T> {
    private String eventType; // 事件类型：heartbeat, status, notification等
    private T data;      // 事件数据
    private LocalDateTime timestamp; // 时间戳
    
    public SseEvent(String eventType, T data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}