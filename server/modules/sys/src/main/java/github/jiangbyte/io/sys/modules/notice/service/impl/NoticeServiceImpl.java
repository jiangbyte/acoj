package github.jiangbyte.io.sys.modules.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.security.HtmlSanitizer;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.mybatis.dialect.DbDialect;
import github.jiangbyte.io.common.mybatis.util.LikeQueries;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.sys.modules.notice.convert.SysNoticeConvert;
import github.jiangbyte.io.sys.modules.notice.entity.SysNotice;
import github.jiangbyte.io.sys.modules.notice.entity.SysNoticeRead;
import github.jiangbyte.io.sys.modules.notice.mapper.SysNoticeMapper;
import github.jiangbyte.io.sys.modules.notice.mapper.SysNoticeReadMapper;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeAddParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeEditParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticePageParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticePinParam;
import github.jiangbyte.io.sys.modules.notice.param.SysNoticeReadParam;
import github.jiangbyte.io.sys.modules.notice.service.NoticeService;
import github.jiangbyte.io.sys.support.MessageAuthSupport;
import github.jiangbyte.io.sys.support.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * {@link github.jiangbyte.io.sys.modules.notice.service.NoticeService} 实现：消息校验与发布、可见性过滤、已读落库与未读统计。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements NoticeService {

    private final DbDialect dbDialect;

    private final SysNoticeReadMapper readMapper;
    private final SysNoticeConvert noticeConvert;

    @Transactional
    @Override
    public void create(SysNoticeAddParam param) {
        // 校验并规范化 kind/目标范围
        // 应用类型默认值与状态后保存
        normalizeAndValidate(param);
        SysNotice entity = noticeConvert.toEntity(param);
        applyKindDefaults(entity, param.getKind());
        String status = resolveStatus(param.getStatus());
        entity.setStatus(status);
        if (MessageConstants.PUBLISHED.equals(status) && entity.getPublishAt() == null) {
            entity.setPublishAt(OffsetDateTime.now());
        }
        if (entity.getViewCount() == null) {
            entity.setViewCount(0);
        }
        if (entity.getExtra() == null) {
            entity.setExtra(Map.of());
        }
        if (entity.getPublishLocations() == null) {
            entity.setPublishLocations(new HashMap<>());
        }
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Transactional
    @Override
    public void update(SysNoticeEditParam param) {
        // 加载消息并校验存在
        // 覆盖字段、应用默认值后更新
        SysNotice entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "消息不存在");
        }
        normalizeAndValidate(param);
        AuditSnapshots.before(entity);
        noticeConvert.update(param, entity);
        applyKindDefaults(entity, param.getKind());
        String status = resolveStatus(param.getStatus());
        entity.setStatus(status);
        if (MessageConstants.PUBLISHED.equals(status) && entity.getPublishAt() == null) {
            entity.setPublishAt(OffsetDateTime.now());
        }
        if (entity.getExtra() == null) {
            entity.setExtra(Map.of());
        }
        if (entity.getPublishLocations() == null) {
            entity.setPublishLocations(new HashMap<>());
        }
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Transactional
    @Override
    public void delete(IdsParam param) {
        // 分批删除已读记录后删除消息
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysNotice> entities = this.listByIds(ids);
        AuditSnapshots.deletedAll(entities);
        for (List<String> batch : BatchPartition.partition(ids)) {
            readMapper.delete(Wrappers.<SysNoticeRead>lambdaQuery().in(SysNoticeRead::getNoticeId, batch));
        }
        this.removeByIds(ids);
    }

    @ReadDataSource
    @Override
    public SysNotice detail(String id) {
        // 按 ID 查询，不存在则 404
        SysNotice entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "消息不存在");
        }
        return entity;
    }

    @ReadDataSource
    @Override
    public Page<SysNotice> page(SysNoticePageParam param) {
        // 按标题/状态/类型构建条件并分页
        LambdaQueryWrapper<SysNotice> qw = Wrappers.<SysNotice>lambdaQuery()
                .like(StringUtils.hasText(param.getTitle()), SysNotice::getTitle, LikeQueries.keyword(param.getTitle()))
                .eq(StringUtils.hasText(param.getStatus()), SysNotice::getStatus, param.getStatus())
                .eq(StringUtils.hasText(param.getKind()), SysNotice::getKind,
                        StringUtils.hasText(param.getKind()) ? param.getKind().toUpperCase(Locale.ROOT) : null)
                .orderByDesc(SysNotice::getCreatedAt);
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), qw);
    }

    @Transactional
    @Override
    public void publish(IdsParam param) {
        // 分批加载消息并标记已发布
        // 写入发布人与发布时间
        LoginUser user = MessageAuthSupport.requireUser();
        OffsetDateTime now = OffsetDateTime.now();
        String senderAccountType = MessageAuthSupport.accountType(user);
        String senderAccountId = user.getAccountId();
        for (List<String> batch : BatchPartition.partition(param.getIds())) {
            List<SysNotice> entities = this.listByIds(batch);
            for (SysNotice entity : entities) {
                AuditSnapshots.before(entity);
                entity.setStatus(MessageConstants.PUBLISHED);
                entity.setPublishAt(now);
                entity.setSenderAccountType(senderAccountType);
                entity.setSenderAccountId(senderAccountId);
                AuditSnapshots.after(entity);
            }
            if (!entities.isEmpty()) {
                this.updateBatchById(entities);
            }
        }
    }

    @Transactional
    @Override
    public void revoke(IdsParam param) {
        // 分批标记撤回并记录撤回时间
        OffsetDateTime now = OffsetDateTime.now();
        for (List<String> batch : BatchPartition.partition(param.getIds())) {
            List<SysNotice> entities = this.listByIds(batch);
            for (SysNotice entity : entities) {
                AuditSnapshots.before(entity);
                entity.setStatus(MessageConstants.REVOKED);
                entity.setRevokedAt(now);
                AuditSnapshots.after(entity);
            }
            if (!entities.isEmpty()) {
                this.updateBatchById(entities);
            }
        }
    }

    @Transactional
    @Override
    public void pin(SysNoticePinParam param) {
        // 仅公告允许置顶；更新置顶字段
        SysNotice entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "消息不存在");
        }
        if (!MessageConstants.KIND_ANNOUNCEMENT.equals(entity.getKind())) {
            throw new BizException("仅公告支持置顶");
        }
        AuditSnapshots.before(entity);
        entity.setIsPinned(param.getIsPinned());
        entity.setPinnedUntil(param.getPinnedUntil());
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @ReadDataSource
    @Override
    public Page<SysNotice> portalList(SysNoticePageParam param) {
        // 解析当前用户（可匿名）后查已发布公告
        Optional<LoginUser> userOpt = LoginHelper.currentUser();
        String accountType = AccountType.PORTAL.name();
        String accountId = null;
        if (userOpt.isPresent()) {
            LoginUser user = userOpt.get();
            accountType = MessageAuthSupport.accountType(user);
            accountId = user.getAccountId();
        }
        return pagePublished(param, accountType, accountId, MessageConstants.KIND_ANNOUNCEMENT);
    }

    @ReadDataSource
    @Override
    public Page<SysNotice> myPage(SysNoticePageParam param) {
        // 按当前用户可见性分页已发布消息
        LoginUser user = MessageAuthSupport.requireUser();
        String kind = StringUtils.hasText(param.getKind()) ? param.getKind().toUpperCase(Locale.ROOT) : null;
        return pagePublished(param, MessageAuthSupport.accountType(user), user.getAccountId(), kind);
    }

    @Transactional
    @Override
    public SysNotice myDetail(String id) {
        // 校验已发布且对当前用户可见
        // 公告累加浏览并标记已读
        LoginUser user = MessageAuthSupport.requireUser();
        SysNotice entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "消息不存在");
        }
        if (!MessageConstants.PUBLISHED.equals(entity.getStatus())) {
            throw new BizException(404, "消息不存在");
        }
        if (!visibleTo(entity, MessageAuthSupport.accountType(user), user.getAccountId())) {
            throw new BizException(404, "消息不存在");
        }
        if (MessageConstants.KIND_ANNOUNCEMENT.equals(entity.getKind())) {
            entity.setViewCount((entity.getViewCount() == null ? 0 : entity.getViewCount()) + 1);
            this.updateById(entity);
        }
        markReadInternal(List.of(id), MessageAuthSupport.accountType(user), user.getAccountId());
        entity.setIsRead(true);
        return entity;
    }

    @ReadDataSource
    @Override
    public int unreadCount() {
        // 统计当前用户未读可见消息数
        LoginUser user = MessageAuthSupport.requireUser();
        return getBaseMapper().countUnread(
                MessageAuthSupport.accountType(user),
                user.getAccountId(),
                null,
                OffsetDateTime.now());
    }

    @Transactional
    @Override
    public void markRead(SysNoticeReadParam param) {
        // 将指定消息写入已读表
        LoginUser user = MessageAuthSupport.requireUser();
        List<String> ids = param.getIds() == null ? List.of() : param.getIds();
        List<String> titles = List.of();
        if (!ids.isEmpty()) {
            titles = this.listByIds(ids).stream()
                    .map(SysNotice::getTitle)
                    .filter(StringUtils::hasText)
                    .toList();
        }
        AuditSnapshots.after(Map.of("消息", titles));
        markReadInternal(ids, MessageAuthSupport.accountType(user), user.getAccountId());
    }

    @Transactional
    @Override
    public void markAllRead() {
        LoginUser user = MessageAuthSupport.requireUser();
        String accountType = MessageAuthSupport.accountType(user);
        String accountId = user.getAccountId();
        int unread = getBaseMapper().countUnread(accountType, accountId, null, OffsetDateTime.now());
        AuditSnapshots.after(Map.of("已读数量", unread));
        OffsetDateTime now = OffsetDateTime.now();
        final int batchSize = 500;
        int offset = 0;
        while (true) {
            List<String> publishedIds = getBaseMapper().listVisiblePublishedIdsPage(
                    accountType, accountId, null, now, offset, batchSize);
            if (publishedIds == null || publishedIds.isEmpty()) {
                break;
            }
            markReadInternal(publishedIds, accountType, accountId);
            if (publishedIds.size() < batchSize) {
                break;
            }
            offset += batchSize;
        }
    }

    private Page<SysNotice> pagePublished(
            SysNoticePageParam param, String accountType, String accountId, String kind) {
        // 构建已发布且未过期条件
        // 叠加可见性过滤并分页
        // 有账号时回填已读标记
        OffsetDateTime now = OffsetDateTime.now();
        LambdaQueryWrapper<SysNotice> qw = Wrappers.<SysNotice>lambdaQuery()
                .eq(SysNotice::getStatus, MessageConstants.PUBLISHED)
                .and(w -> w.ne(SysNotice::getKind, MessageConstants.KIND_ANNOUNCEMENT)
                        .or().isNull(SysNotice::getExpireAt)
                        .or().gt(SysNotice::getExpireAt, now))
                .eq(StringUtils.hasText(kind), SysNotice::getKind, kind)
                .orderByDesc(SysNotice::getIsPinned)
                .orderByDesc(SysNotice::getPublishAt);
        applyVisibilitySql(qw, accountType, accountId);
        Page<SysNotice> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), qw);
        if (StringUtils.hasText(accountId) && !page.getRecords().isEmpty()) {
            Set<String> readIds = loadReadIds(
                    accountType, accountId, page.getRecords().stream().map(SysNotice::getId).toList());
            for (SysNotice notice : page.getRecords()) {
                notice.setIsRead(readIds.contains(notice.getId()));
            }
        }
        return page;
    }

    private void applyVisibilitySql(LambdaQueryWrapper<SysNotice> qw, String accountType, String accountId) {
        // 全部/按类型：匹配账户类型 JSON
        // 指定用户：匹配账户 ID JSON
        qw.and(w -> {
            w.and(typeScope -> typeScope
                    .in(SysNotice::getTargetScope, List.of(
                            MessageConstants.TARGET_ALL, MessageConstants.TARGET_ACCOUNT_TYPE))
                    .and(typeMatch -> typeMatch
                            .isNull(SysNotice::getTargetAccountTypes)
                            .or()
                            .apply(dbDialect.jsonArrayEmptyOrContainsApply("target_account_types"), accountType)));
            if (StringUtils.hasText(accountId)) {
                w.or(specific -> specific
                        .eq(SysNotice::getTargetScope, MessageConstants.TARGET_SPECIFIC)
                        .apply(dbDialect.jsonArrayContainsApply("target_account_ids"), accountId));
            }
        });
    }

    private boolean visibleTo(SysNotice n, String accountType, String accountId) {
        // 解析目标范围与类型匹配
        // 指定用户则校验账户 ID 列表
        String scope = n.getTargetScope() == null ? MessageConstants.TARGET_ALL : n.getTargetScope();
        List<String> types = n.getTargetAccountTypes();
        boolean typeMatch = CollectionUtils.isEmpty(types) || types.contains(accountType);
        if (MessageConstants.TARGET_ALL.equals(scope) || MessageConstants.TARGET_ACCOUNT_TYPE.equals(scope)) {
            return typeMatch;
        }
        if (MessageConstants.TARGET_SPECIFIC.equals(scope)) {
            return StringUtils.hasText(accountId)
                    && n.getTargetAccountIds() != null
                    && n.getTargetAccountIds().contains(accountId);
        }
        return false;
    }

    private void markReadInternal(List<String> noticeIds, String accountType, String accountId) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return;
        }
        List<String> distinctIds = noticeIds.stream().filter(StringUtils::hasText).distinct().toList();
        if (distinctIds.isEmpty()) {
            return;
        }
        Set<String> existing = loadReadIds(accountType, accountId, distinctIds);
        OffsetDateTime now = OffsetDateTime.now();
        List<SysNoticeRead> batch = new ArrayList<>();
        for (String noticeId : distinctIds) {
            if (existing.contains(noticeId)) {
                continue;
            }
            SysNoticeRead read = new SysNoticeRead();
            read.setNoticeId(noticeId);
            read.setAccountType(accountType);
            read.setAccountId(accountId);
            read.setReadAt(now);
            batch.add(read);
        }
        for (List<SysNoticeRead> chunk : BatchPartition.partition(batch)) {
            if (!chunk.isEmpty()) {
                readMapper.insertBatch(chunk);
            }
        }
    }

    private Set<String> loadReadIds(String accountType, String accountId, List<String> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Set.of();
        }
        Set<String> readIds = new HashSet<>();
        for (List<String> batch : BatchPartition.partition(noticeIds)) {
            readMapper.selectList(Wrappers.<SysNoticeRead>lambdaQuery()
                            .eq(SysNoticeRead::getAccountType, accountType)
                            .eq(SysNoticeRead::getAccountId, accountId)
                            .in(SysNoticeRead::getNoticeId, batch))
                    .forEach(item -> readIds.add(item.getNoticeId()));
        }
        return readIds;
    }

    private void normalizeAndValidate(SysNoticeAddParam param) {
        // 规范化并校验 kind
        // 规范化目标范围与账户类型/用户
        // 按类型校验发布位置或通知分类
        String kind = param.getKind() == null ? "" : param.getKind().toUpperCase(Locale.ROOT);
        if (!MessageConstants.KIND_NOTIFICATION.equals(kind)
                && !MessageConstants.KIND_ANNOUNCEMENT.equals(kind)) {
            throw new BizException("kind 必须是 NOTIFICATION 或 ANNOUNCEMENT");
        }
        param.setKind(kind);

        String scope = param.getTargetScope() == null
                ? MessageConstants.TARGET_ALL
                : param.getTargetScope().toUpperCase(Locale.ROOT);
        if (!MessageConstants.TARGET_ALL.equals(scope)
                && !MessageConstants.TARGET_ACCOUNT_TYPE.equals(scope)
                && !MessageConstants.TARGET_SPECIFIC.equals(scope)) {
            throw new BizException("目标范围仅支持全部 / 按账户类型 / 指定用户");
        }
        param.setTargetScope(scope);
        if (CollectionUtils.isEmpty(param.getTargetAccountTypes())) {
            throw new BizException("必须选择目标账户类型");
        }
        if (MessageConstants.TARGET_SPECIFIC.equals(scope)
                && CollectionUtils.isEmpty(param.getTargetAccountIds())) {
            throw new BizException("指定用户时必须选择目标用户");
        }

        if (MessageConstants.KIND_ANNOUNCEMENT.equals(kind)) {
            if (!hasEnabledPublishLocation(param.getPublishLocations())) {
                throw new BizException("公告必须选择至少一个发布位置");
            }
        } else {
            if (!StringUtils.hasText(param.getCategory())) {
                throw new BizException("通知必须选择分类");
            }
            if (param.getPublishLocations() == null) {
                param.setPublishLocations(new HashMap<>());
            }
            param.setIsPinned(false);
            param.setPinnedUntil(null);
            param.setExpireAt(null);
        }
        param.setContent(HtmlSanitizer.sanitize(param.getContentType(), param.getContent()));
    }

    private void applyKindDefaults(SysNotice entity, String kind) {
        entity.setKind(kind);
        if (MessageConstants.KIND_NOTIFICATION.equals(kind)) {
            entity.setIsPinned(false);
            entity.setPinnedUntil(null);
            entity.setExpireAt(null);
            if (entity.getPublishLocations() == null) {
                entity.setPublishLocations(new HashMap<>());
            }
        } else if (entity.getIsPinned() == null) {
            entity.setIsPinned(false);
        }
        if (entity.getViewCount() == null) {
            entity.setViewCount(0);
        }
    }

    private String resolveStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return MessageConstants.DRAFT;
        }
        String normalized = status.toUpperCase(Locale.ROOT);
        if ("ENABLED".equals(normalized) || "ENABLE".equals(normalized)) {
            return MessageConstants.DRAFT;
        }
        if (MessageConstants.DRAFT.equals(normalized)
                || MessageConstants.PUBLISHED.equals(normalized)
                || MessageConstants.REVOKED.equals(normalized)) {
            return normalized;
        }
        return MessageConstants.DRAFT;
    }

    private boolean hasEnabledPublishLocation(Map<String, Object> publishLocations) {
        if (publishLocations == null || publishLocations.isEmpty()) {
            return false;
        }
        return publishLocations.values().stream().anyMatch(v -> {
            if (v instanceof Boolean b) {
                return b;
            }
            return v != null && Boolean.parseBoolean(String.valueOf(v));
        });
    }
}
