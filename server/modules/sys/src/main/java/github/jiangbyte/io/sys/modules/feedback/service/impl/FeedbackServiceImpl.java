package github.jiangbyte.io.sys.modules.feedback.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.satoken.model.LoginUser;
import github.jiangbyte.io.sys.modules.feedback.convert.SysFeedbackConvert;
import github.jiangbyte.io.sys.modules.feedback.entity.SysFeedback;
import github.jiangbyte.io.sys.modules.feedback.mapper.SysFeedbackMapper;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackAddParam;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackEditParam;
import github.jiangbyte.io.sys.modules.feedback.param.SysFeedbackPageParam;
import github.jiangbyte.io.sys.modules.feedback.result.SysFeedbackAttachmentResult;
import github.jiangbyte.io.sys.modules.feedback.service.FeedbackService;
import github.jiangbyte.io.sys.support.MessageAuthSupport;
import github.jiangbyte.io.sys.support.MessageConstants;
import github.jiangbyte.io.sys.file.FileApi;
import github.jiangbyte.io.sys.file.FileInfo;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.profile.admin.ProfileUserAdminApi;
import github.jiangbyte.io.profile.admin.ProfileUserAdminInfo;
import github.jiangbyte.io.profile.portal.ProfileUserPortalApi;
import github.jiangbyte.io.profile.portal.ProfileUserPortalInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link github.jiangbyte.io.sys.modules.feedback.service.FeedbackService} 实现：反馈持久化、附件规范化与提交人资料补全。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<SysFeedbackMapper, SysFeedback> implements FeedbackService {

    private final SysFeedbackConvert feedbackConvert;
    private final FileApi fileApi;
    private final ProfileUserAdminApi adminUserProfileApi;
    private final ProfileUserPortalApi portalUserProfileApi;

    @ReadDataSource
    @Override
    public Page<SysFeedback> page(SysFeedbackPageParam param) {
        // 按状态/分类分页查询反馈
        // 补全附件与提交人展示信息
        Page<SysFeedback> page = this.getBaseMapper().selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysFeedback>lambdaQuery()
                        .like(StringUtils.hasText(param.getTitle()), SysFeedback::getTitle, param.getTitle())
                        .eq(StringUtils.hasText(param.getStatus()), SysFeedback::getStatus, param.getStatus())
                        .eq(StringUtils.hasText(param.getCategory()), SysFeedback::getCategory, param.getCategory())
                        .eq(StringUtils.hasText(param.getSubmitterAccountType()),
                                SysFeedback::getSubmitterAccountType, param.getSubmitterAccountType())
                        .orderByDesc(SysFeedback::getCreatedAt));
        enrichMany(page.getRecords());
        return page;
    }

    @ReadDataSource
    @Override
    public SysFeedback detail(String id) {
        // 按 ID 加载反馈，不存在则 404
        // 补全附件与提交人后返回
        SysFeedback entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "反馈不存在");
        }
        return enrichOne(entity);
    }

    @Transactional
    @Override
    public void update(SysFeedbackEditParam param) {
        // 校验反馈存在后更新状态
        // 若有回复则记录回复人与时间
        LoginUser user = MessageAuthSupport.requireUser();
        SysFeedback entity = this.getById(param.getId());
        if (entity == null) {
            throw new BizException(404, "反馈不存在");
        }
        AuditSnapshots.before(entity);
        if (StringUtils.hasText(param.getStatus())) {
            entity.setStatus(param.getStatus());
        }
        if (param.getReply() != null) {
            entity.setReply(param.getReply());
            entity.setRepliedBy(user.getAccountId());
            entity.setRepliedAt(OffsetDateTime.now());
        }
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Transactional
    @Override
    public void delete(IdsParam param) {
        // 空 ID 列表直接返回
        // 按 ID 批量删除反馈
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysFeedback> entities = this.listByIds(ids);
        AuditSnapshots.deletedAll(entities);
        this.removeByIds(ids);
    }

    @Transactional
    @Override
    public void submit(SysFeedbackAddParam param) {
        // 转换参数并补默认标题/分类
        // 规范化附件对象名后落库为待处理
        LoginUser user = MessageAuthSupport.requireUser();
        SysFeedback entity = feedbackConvert.toEntity(param);
        if (!StringUtils.hasText(entity.getTitle())) {
            String content = entity.getContent();
            entity.setTitle(content != null && content.length() > 64
                    ? content.substring(0, 64)
                    : content);
        }
        if (!StringUtils.hasText(entity.getCategory())) {
            entity.setCategory("GENERAL");
        }
        entity.setAttachObjectNames(normalizeAttachNames(entity.getAttachObjectNames()));
        entity.setStatus(MessageConstants.FEEDBACK_PENDING);
        entity.setSubmitterAccountType(MessageAuthSupport.accountType(user));
        entity.setSubmitterAccountId(user.getAccountId());
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @ReadDataSource
    @Override
    public Page<SysFeedback> myPage(SysFeedbackPageParam param) {
        // 按当前提交人过滤并分页
        // 补全附件与提交人展示信息
        LoginUser user = MessageAuthSupport.requireUser();
        Page<SysFeedback> page = this.getBaseMapper().selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysFeedback>lambdaQuery()
                        .eq(SysFeedback::getSubmitterAccountType, MessageAuthSupport.accountType(user))
                        .eq(SysFeedback::getSubmitterAccountId, user.getAccountId())
                        .orderByDesc(SysFeedback::getCreatedAt));
        enrichMany(page.getRecords());
        return page;
    }

    @ReadDataSource
    @Override
    public SysFeedback myDetail(String id) {
        // 校验反馈存在且归属当前用户
        // 补全后返回详情
        LoginUser user = MessageAuthSupport.requireUser();
        SysFeedback entity = this.getById(id);
        if (entity == null) {
            throw new BizException(404, "反馈不存在");
        }
        if (!(MessageAuthSupport.accountType(user).equals(entity.getSubmitterAccountType())
                && user.getAccountId().equals(entity.getSubmitterAccountId()))) {
            throw new BizException(403, "无权查看");
        }
        return enrichOne(entity);
    }

    private List<String> normalizeAttachNames(List<String> raw) {
        // 规范化对象名并去重
        // 校验附件文件均存在
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        for (String value : raw) {
            String normalized = fileApi.normalizeObjectName(value);
            if (StringUtils.hasText(normalized) && !names.contains(normalized)) {
                names.add(normalized);
            }
        }
        if (names.isEmpty()) {
            return names;
        }
        Map<String, FileInfo> found = fileApi.listByObjectNames(names).stream()
                .collect(Collectors.toMap(FileInfo::getObjectName, f -> f, (a, b) -> a, LinkedHashMap::new));
        List<String> missing = names.stream().filter(n -> !found.containsKey(n)).toList();
        if (!missing.isEmpty()) {
            throw new BizException(400, "附件文件不存在");
        }
        return names;
    }

    private SysFeedback enrichOne(SysFeedback entity) {
        enrichMany(List.of(entity));
        return entity;
    }

    private void enrichMany(List<SysFeedback> entities) {
        // 软规范化附件名并批量查文件
        // 组装附件结果后补全提交人
        if (entities == null || entities.isEmpty()) {
            return;
        }
        List<String> allNames = new ArrayList<>();
        for (SysFeedback entity : entities) {
            List<String> names = normalizeAttachNamesSoft(entity.getAttachObjectNames());
            entity.setAttachObjectNames(names);
            allNames.addAll(names);
        }
        Map<String, FileInfo> fileMap = fileApi.listByObjectNames(allNames).stream()
                .collect(Collectors.toMap(FileInfo::getObjectName, f -> f, (a, b) -> a, LinkedHashMap::new));
        for (SysFeedback entity : entities) {
            List<SysFeedbackAttachmentResult> attachments = new ArrayList<>();
            for (String objectName : entity.getAttachObjectNames()) {
                FileInfo file = fileMap.get(objectName);
                SysFeedbackAttachmentResult item = new SysFeedbackAttachmentResult();
                item.setObjectName(objectName);
                if (file != null) {
                    item.setId(file.getId());
                    item.setOriginalName(file.getOriginalName());
                    item.setContentType(file.getContentType());
                    item.setSize(file.getSize());
                }
                item.setUrl(fileApi.resolveUrl(objectName));
                attachments.add(item);
            }
            entity.setAttachments(attachments);
        }
        enrichSubmitters(entities);
    }

    private List<String> normalizeAttachNamesSoft(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        for (String value : raw) {
            String normalized = fileApi.normalizeObjectName(value);
            if (StringUtils.hasText(normalized) && !names.contains(normalized)) {
                names.add(normalized);
            }
        }
        return names;
    }

    private void enrichSubmitters(List<SysFeedback> entities) {
        // 按账户类型归集提交人 ID
        // 批量拉取资料并回填头像/昵称
        Map<String, List<String>> idsByType = new HashMap<>();
        for (SysFeedback entity : entities) {
            if (!StringUtils.hasText(entity.getSubmitterAccountId())
                    || !StringUtils.hasText(entity.getSubmitterAccountType())) {
                continue;
            }
            String type = entity.getSubmitterAccountType().trim().toUpperCase(Locale.ROOT);
            idsByType.computeIfAbsent(type, key -> new ArrayList<>()).add(entity.getSubmitterAccountId());
        }
        Map<String, ProfileUserAdminInfo> adminProfiles = new HashMap<>();
        Map<String, ProfileUserPortalInfo> portalProfiles = new HashMap<>();
        Map<String, String> adminDisplayNames = new HashMap<>();
        Map<String, String> portalDisplayNames = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : idsByType.entrySet()) {
            List<String> ids = entry.getValue().stream().distinct().toList();
            if (AccountType.ADMIN.name().equals(entry.getKey())) {
                adminProfiles.putAll(adminUserProfileApi.getProfiles(ids));
                adminDisplayNames.putAll(adminUserProfileApi.getDisplayNames(ids));
            } else if (AccountType.PORTAL.name().equals(entry.getKey())) {
                portalProfiles.putAll(portalUserProfileApi.getProfiles(ids));
                portalDisplayNames.putAll(portalUserProfileApi.getDisplayNames(ids));
            }
        }
        for (SysFeedback entity : entities) {
            if (!StringUtils.hasText(entity.getSubmitterAccountId())) {
                continue;
            }
            String type = StringUtils.hasText(entity.getSubmitterAccountType())
                    ? entity.getSubmitterAccountType().trim().toUpperCase(Locale.ROOT)
                    : "";
            if (AccountType.ADMIN.name().equals(type)) {
                ProfileUserAdminInfo profile = adminProfiles.get(entity.getSubmitterAccountId());
                if (profile == null) {
                    continue;
                }
                entity.setSubmitterAvatar(fileApi.resolveUrl(profile.getAvatar()));
                entity.setSubmitterNickname(adminDisplayNames.get(entity.getSubmitterAccountId()));
            } else if (AccountType.PORTAL.name().equals(type)) {
                ProfileUserPortalInfo profile = portalProfiles.get(entity.getSubmitterAccountId());
                if (profile == null) {
                    continue;
                }
                entity.setSubmitterAvatar(fileApi.resolveUrl(profile.getAvatar()));
                entity.setSubmitterNickname(portalDisplayNames.get(entity.getSubmitterAccountId()));
            }
        }
    }
}
