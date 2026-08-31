package github.jiangbyte.io.oj.modules.judge.heartbeat;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 进程内 nonce 去重（TTL），防止心跳重放。
 *
 * Author: Charlie
 */
@Component
public class SparkSandboxNonceCache {

    private final ConcurrentHashMap<String, Long> items = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final int maxEntries;

    public SparkSandboxNonceCache() {
        this(600_000L, 100_000);
    }

    public SparkSandboxNonceCache(long ttlMillis, int maxEntries) {
        this.ttlMillis = ttlMillis;
        this.maxEntries = maxEntries;
    }

    /** 首次见到返回 true 并记录；重复或非法返回 false。 */
    public boolean accept(String nonce) {
        if (nonce == null || nonce.isBlank() || nonce.length() > 128) {
            return false;
        }
        long now = System.currentTimeMillis();
        purge(now);
        Long prev = items.putIfAbsent(nonce, now + ttlMillis);
        if (prev != null) {
            return false;
        }
        if (items.size() > maxEntries) {
            purge(now);
            while (items.size() > maxEntries) {
                Iterator<Map.Entry<String, Long>> it = items.entrySet().iterator();
                if (!it.hasNext()) {
                    break;
                }
                it.next();
                it.remove();
            }
        }
        return true;
    }

    private void purge(long now) {
        items.entrySet().removeIf(e -> e.getValue() <= now);
    }
}
