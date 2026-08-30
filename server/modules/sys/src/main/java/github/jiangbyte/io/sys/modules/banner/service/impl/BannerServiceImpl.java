package github.jiangbyte.io.sys.modules.banner.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.security.SafeLinkValidator;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.sys.file.FileApi;
import github.jiangbyte.io.sys.modules.banner.convert.SysBannerConvert;
import github.jiangbyte.io.sys.modules.banner.entity.SysBanner;
import github.jiangbyte.io.sys.modules.banner.mapper.SysBannerMapper;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerAddParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerEditParam;
import github.jiangbyte.io.sys.modules.banner.param.SysBannerPageParam;
import github.jiangbyte.io.sys.modules.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Banner 服务实现：维护与按窗口筛选。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<SysBannerMapper, SysBanner> implements BannerService {

    /** Redis 中 Banner 互动计数增量哈希键（对齐 hei-fastapi banner_interaction_delta_key）。 */
    public static final String INTERACTION_DELTA_KEY = "hei:banner:interaction:deltas";

    private final SysBannerConvert bannerConvert;
    private final FileApi fileApi;
    private final StringRedisTemplate stringRedisTemplate;
    private final DbDialect dbDialect;

    @Override
    @Transactional
    public void create(SysBannerAddParam param) {
        try {
            SafeLinkValidator.validateBannerLink(param.getLinkType(), param.getUrl());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ex.getMessage());
        }
        // 入参转实体
        SysBanner banner = bannerConvert.toEntity(param);
        if (StringUtils.hasText(banner.getImage())) {
            banner.setImage(fileApi.normalizeObjectName(banner.getImage()));
        }
        banner.setInteractionCount(0L);
        this.save(banner);
        AuditSnapshots.created(banner);
    }

    @Override
    @Transactional
    public void update(SysBannerEditParam param) {
        try {
            SafeLinkValidator.validateBannerLink(param.getLinkType(), param.getUrl());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ex.getMessage());
        }
        // 按主键加载
        SysBanner banner = this.getById(param.getId());
        if (banner == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Banner not found");
        }
        AuditSnapshots.before(banner);
        bannerConvert.update(param, banner);
        if (StringUtils.hasText(banner.getImage())) {
            banner.setImage(fileApi.normalizeObjectName(banner.getImage()));
        }
        this.updateById(banner);
        AuditSnapshots.after(banner);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysBanner> banners = this.listByIds(ids);
        AuditSnapshots.deletedAll(banners);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysBanner detail(String id) {
        // 按主键加载
        SysBanner banner = this.getById(id);
        if (banner == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Banner not found");
        }
        return withResolvedImageUrl(banner);
    }

    @Override
    @ReadDataSource
    public Page<SysBanner> page(SysBannerPageParam param) {
        // 分页查询
        Page<SysBanner> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysBanner>lambdaQuery()
                        .apply(StringUtils.hasText(param.getTargetAccountType()),
                                dbDialect.jsonArrayContainsApply("target_account_types"), param.getTargetAccountType())
                        .eq(StringUtils.hasText(param.getCategory()), SysBanner::getCategory, param.getCategory())
                        .eq(StringUtils.hasText(param.getType()), SysBanner::getType, param.getType())
                        .eq(StringUtils.hasText(param.getPosition()), SysBanner::getPosition, param.getPosition())
                        .eq(StringUtils.hasText(param.getStatus()), SysBanner::getStatus, param.getStatus())
                        .orderByAsc(SysBanner::getSort)
                        .orderByDesc(SysBanner::getCreatedAt));
        page.getRecords().forEach(this::withResolvedImageUrl);
        return page;
    }

    @Override
    @ReadDataSource
    public List<SysBanner> portalList(String position, String category, String type) {
        return listVisible(position, category, type, "PORTAL");
    }

    @Override
    @ReadDataSource
    public List<SysBanner> adminList(String position, String category, String type) {
        return listVisible(position, category, type, "ADMIN");
    }

    private List<SysBanner> listVisible(String position, String category, String type, String accountType) {
        if (!StringUtils.hasText(position)) {
            throw new BizException("position is required");
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<SysBanner> list = getBaseMapper().selectList(Wrappers.<SysBanner>lambdaQuery()
                .eq(SysBanner::getPosition, position)
                .eq(SysBanner::getStatus, "ENABLED")
                .apply(dbDialect.jsonArrayContainsApply("target_account_types"), accountType)
                .eq(StringUtils.hasText(category), SysBanner::getCategory, category)
                .eq(StringUtils.hasText(type), SysBanner::getType, type)
                .and(wrapper -> wrapper.isNull(SysBanner::getStartAt).or().le(SysBanner::getStartAt, now))
                .and(wrapper -> wrapper.isNull(SysBanner::getEndAt).or().ge(SysBanner::getEndAt, now))
                .orderByAsc(SysBanner::getSort));
        list.forEach(this::withResolvedImageUrl);
        return list;
    }

    private SysBanner withResolvedImageUrl(SysBanner banner) {
        if (banner != null) {
            String resolved = fileApi.resolveUrl(banner.getImage());
            banner.setImageUrl(StringUtils.hasText(resolved) ? resolved : banner.getImage());
        }
        return banner;
    }

    @Override
    @Transactional
    public void interaction(String id) {
        // 按主键加载
        SysBanner banner = this.getById(id);
        if (banner == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Banner not found");
        }
        boolean portalTarget = banner.getTargetAccountTypes() != null
                && banner.getTargetAccountTypes().stream().anyMatch("PORTAL"::equalsIgnoreCase);
        if (!"ENABLED".equalsIgnoreCase(banner.getStatus()) || !portalTarget) {
            throw new BizException("Banner is not publicly visible");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (banner.getStartAt() != null && banner.getStartAt().isAfter(now)) {
            throw new BizException("Banner is not publicly visible");
        }
        if (banner.getEndAt() != null && banner.getEndAt().isBefore(now)) {
            throw new BizException("Banner is not publicly visible");
        }
        String subject = StringUtils.hasText(banner.getTitle())
                ? banner.getTitle()
                : (StringUtils.hasText(banner.getDescription()) ? banner.getDescription() : banner.getId());
        AuditSnapshots.subject(subject);
        AuditSnapshots.after(Map.of("展示图", subject));
        // 计数先入 Redis 增量，由 bannerFlushInteractions 周期任务刷入 DB（对齐 hei-fastapi）
        stringRedisTemplate.opsForHash().increment(INTERACTION_DELTA_KEY, id, 1L);
    }
}
