package io.charlie.app.core.activity;

import io.charlie.app.core.sse.entity.SseEvent;
import io.charlie.app.core.sse.enums.EventEnums;
import io.charlie.app.core.sse.utils.SseUtil;
import io.charlie.galaxy.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "客户端状态控制器")
@Slf4j
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivitiesController {

    private final SseUtil sseUtil;

    /**
     * 建立SSE连接
     */
//    @GetMapping(value = "/connect/{clientId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @GetMapping("/connect/{clientId}")
    public SseEmitter connect(@PathVariable String clientId) {
        return sseUtil.connection(clientId);
    }

    /**
     * 发送消息到指定客户端
     */
    @PostMapping("/message/{clientId}")
    public Result<Void> sendMessage(@PathVariable String clientId, @RequestParam String message) {
        sseUtil.message(clientId, new SseEvent<String>(EventEnums.MESSAGE.getValue(), message));
        return Result.success();
    }

    /**
     * 广播消息到所有客户端
     */
    @PostMapping("/broadcast")
    public Result<Void> broadcast(@RequestParam String message) {
        sseUtil.broadcast(new SseEvent<String>(EventEnums.BROADCAST.getValue(), message));
        return Result.success();
    }

    /**
     * 关闭指定客户端连接
     */
    @PostMapping("/disconnect/{clientId}")
    public Result<Void> disconnect(@PathVariable String clientId) {
        sseUtil.close(clientId);
        return Result.success();
    }

    /**
     * 处理心跳响应
     */
    @PostMapping("/heartbeat/{clientId}")
    public Result<Void> heartbeat(@PathVariable String clientId) {
        sseUtil.clientHeartbeat(clientId);
        return Result.success();
    }
}