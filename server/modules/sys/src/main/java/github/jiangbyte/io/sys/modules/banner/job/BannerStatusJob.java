package github.jiangbyte.io.sys.modules.banner.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import github.jiangbyte.io.common.job.JobHandler;
import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.mapper.SysBannerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Banner 状态定时任务：按 start_at / end_at 激活或过期。
 *
 * Author: Charlie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BannerStatusJob implements JobHandler {

    private final SysBannerMapper bannerMapper;

    @Override
    public String execute(String params) {
        OffsetDateTime now = OffsetDateTime.now();
        int expired = bannerMapper.update(null, Wrappers.<SysBanner>lambdaUpdate()
                .set(SysBanner::getStatus, "DISABLED")
                .set(SysBanner::getUpdatedAt, now)
                .eq(SysBanner::getStatus, "ENABLED")
                .isNotNull(SysBanner::getEndAt)
                .lt(SysBanner::getEndAt, now));

        int activated = bannerMapper.update(null, Wrappers.<SysBanner>lambdaUpdate()
                .set(SysBanner::getStatus, "ENABLED")
                .set(SysBanner::getUpdatedAt, now)
                .eq(SysBanner::getStatus, "DISABLED")
                .isNotNull(SysBanner::getStartAt)
                .le(SysBanner::getStartAt, now)
                .and(w -> w.isNull(SysBanner::getEndAt).or().ge(SysBanner::getEndAt, now)));

        log.info("Banner status sync: expired={}, activated={}", expired, activated);
        return "expired=" + expired + ",activated=" + activated;
    }
}
