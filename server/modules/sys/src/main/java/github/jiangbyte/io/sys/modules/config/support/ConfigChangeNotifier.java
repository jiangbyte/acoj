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

import java.util.Map;
import cn.hutool.core.util.IdUtil;
import java.util.concurrent.atomic.AtomicReference;
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
    private final AtomicReference<Consumer<Void>> invalidateHandler = new AtomicReference<>();

    private RTopic topic;
    private Integer listenerId;

    public void onInvalidate(Consumer<Void> handler) {
        invalidateHandler.set(handler);
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
                Consumer<Void> handler = invalidateHandler.get();
                if (handler != null) {
                    handler.accept(null);
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
