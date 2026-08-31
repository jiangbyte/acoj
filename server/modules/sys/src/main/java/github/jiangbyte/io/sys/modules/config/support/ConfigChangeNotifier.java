package github.jiangbyte.io.sys.modules.config.support;

import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import cn.hutool.core.util.IdUtil;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

/**
 * 配置变更通知器：刷新 RuntimeSettings 等监听方。
 *
 * Author: Charlie
 */
@Component
@RequiredArgsConstructor
public class ConfigChangeNotifier {

    public static final String CHANNEL = "hei:config:changed";

    private static final Logger log = LoggerFactory.getLogger(ConfigChangeNotifier.class);

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final String instanceId = IdUtil.simpleUUID();
    private final List<Consumer<Void>> invalidateHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<Void>> afterReloadHandlers = new CopyOnWriteArrayList<>();

    private RTopic topic;
    private Integer listenerId;

    public void onInvalidate(Consumer<Void> handler) {
        invalidateHandlers.add(handler);
    }

    /** RuntimeSettings 重载后的本地回调（如刷新 StorageEngine 缓存）。 */
    public void onAfterReload(Consumer<Void> handler) {
        afterReloadHandlers.add(handler);
    }

    public void runAfterReloadHandlers() {
        for (Consumer<Void> handler : afterReloadHandlers) {
            try {
                handler.accept(null);
            } catch (Exception ex) {
                log.warn("Config after-reload handler failed", ex);
            }
        }
    }

    @PostConstruct
    public void subscribe() {
        topic = redissonClient.getTopic(CHANNEL);
        listenerId = topic.addListener(String.class, (MessageListener<String>) (channel, message) -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> event = objectMapper.readValue(message, Map.class);
                Object source = event.get("source");
                if (instanceId.equals(String.valueOf(source))) {
                    return;
                }
                for (Consumer<Void> handler : invalidateHandlers) {
                    try {
                        handler.accept(null);
                    } catch (Exception ex) {
                        log.warn("Config invalidate handler failed", ex);
                    }
                }
                log.info("Config cache invalidated from distributed event reason={}", event.get("reason"));
            } catch (Exception ex) {
                log.warn("Failed to handle config change event", ex);
            }
        });
        log.info("Config sync listener started on channel {}", CHANNEL);
    }

    @PreDestroy
    public void unsubscribe() {
        if (topic != null && listenerId != null) {
            topic.removeListener(listenerId);
        }
    }

    public void publish(String reason) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "source", instanceId,
                    "reason", reason == null ? "updated" : reason,
                    "at", java.time.Instant.now().toString()
            ));
            redissonClient.getTopic(CHANNEL).publish(payload);
        } catch (Exception ex) {
            log.warn("Failed to publish config change event", ex);
        }
    }
}
