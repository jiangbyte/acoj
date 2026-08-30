package github.jiangbyte.io.sys.modules.banner.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.mapper.SysBannerMapper;
import github.jiangbyte.io.sys.modules.banner.service.impl.BannerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Banner 互动计数刷库任务：把 Redis 增量写入 sys_banner.interaction_count
 * （对齐 hei-fastapi bannerFlushInteractions）。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BannerFlushInteractionsJob implements JobHandler {

    private final StringRedisTemplate stringRedisTemplate;
    private final SysBannerMapper bannerMapper;

    @Override
    public String execute(String params) {
        Map<Object, Object> raw = stringRedisTemplate.opsForHash()
                .entries(BannerServiceImpl.INTERACTION_DELTA_KEY);
        if (raw == null || raw.isEmpty()) {
            return "flushed=0";
        }

        // 只保留正数增量，非法值忽略
        Map<String, Long> deltas = new HashMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            String id = String.valueOf(entry.getKey());
            try {
                long value = Long.parseLong(String.valueOf(entry.getValue()));
                if (value > 0) {
                    deltas.put(id, value);
                }
            } catch (NumberFormatException ignored) {
                // 忽略非法值
            }
        }
        if (deltas.isEmpty()) {
            stringRedisTemplate.delete(BannerServiceImpl.INTERACTION_DELTA_KEY);
            return "flushed=0";
        }

        for (Map.Entry<String, Long> entry : deltas.entrySet()) {
            bannerMapper.update(null, Wrappers.<SysBanner>lambdaUpdate()
                    .setSql("interaction_count = interaction_count + {0}", entry.getValue())
                    .eq(SysBanner::getId, entry.getKey()));
        }
        stringRedisTemplate.opsForHash().delete(
                BannerServiceImpl.INTERACTION_DELTA_KEY, deltas.keySet().toArray());
        log.info("bannerFlushInteractions flushed={}", deltas.size());
        return "flushed=" + deltas.size();
    }
}
