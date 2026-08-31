package github.jiangbyte.io.sys.modules.config.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.sys.modules.config.convert.SysConfigConvert;
import github.jiangbyte.io.sys.modules.config.entity.SysConfig;
import github.jiangbyte.io.sys.modules.config.mapper.SysConfigMapper;
import github.jiangbyte.io.sys.modules.config.param.SysConfigAddParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigBatchItemParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigBatchSaveParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigEditParam;
import github.jiangbyte.io.sys.modules.config.param.SysConfigPageParam;
import github.jiangbyte.io.sys.modules.config.result.SysConfigResult;
import github.jiangbyte.io.sys.config.RuntimeSettingsHolder;
import github.jiangbyte.io.sys.modules.config.service.ConfigService;
import github.jiangbyte.io.sys.modules.config.support.ConfigChangeNotifier;
import github.jiangbyte.io.sys.modules.config.support.ConfigCryptoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现：加解密、批量保存与变更通知。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ConfigService {

    private static final Set<String> SENSITIVE_CONFIG_KEYS = Set.of(
            "AUTH_DEFAULT_PASSWORD",
            "AUDIT_ALERT_WEBHOOK_SECRET",
            "MAIL_LOCAL_PASSWORD",
            "MAIL_ALIYUN_ACCESS_KEY_SECRET",
            "MAIL_TENCENT_SECRET_KEY",
            "SMS_ALIYUN_ACCESS_KEY_SECRET",
            "SMS_TENCENT_SECRET_KEY",
            "PUSH_DINGTALK_SECRET",
            "PUSH_LARK_SECRET",
            "STORAGE_MINIO_ACCESS_KEY",
            "STORAGE_MINIO_SECRET_KEY",
            "STORAGE_RUSTFS_ACCESS_KEY",
            "STORAGE_RUSTFS_SECRET_KEY",
            "STORAGE_ALIYUN_ACCESS_KEY",
            "STORAGE_ALIYUN_SECRET_KEY",
            "STORAGE_TENCENT_ACCESS_KEY",
            "STORAGE_TENCENT_SECRET_KEY"
    );

    private final SysConfigConvert configConvert;
    private final ConfigChangeNotifier configChangeNotifier;
    private final ConfigCryptoService configCryptoService;
    private volatile ConcurrentHashMap<String, String> valueCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        configChangeNotifier.onInvalidate(ignored -> invalidateLocalCache());
        warmCache();
        RuntimeSettingsHolder.bindLoader(this::snapshot);
    }

    @Override
    @Transactional
    public void create(SysConfigAddParam param) {
        // 校验唯一性
        SysConfig existing = getBaseMapper().selectOne(Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, param.getConfigKey()).last("limit 1"));
        if (existing != null) {
            throw new BizException("Config key already exists");
        }
        SysConfig config = configConvert.toEntity(param);
        if (!StringUtils.hasText(config.getValueType())) {
            config.setValueType("STRING");
        }
        if (config.getIsBuiltin() == null) {
            config.setIsBuiltin(false);
        }
        if (config.getExtJson() == null) {
            config.setExtJson(Map.of());
        }
        config.setConfigValue(encryptValue(config.getConfigKey(), config.getConfigValue()));
        this.save(config);
        AuditSnapshots.created(config);
        afterMutation("create", Map.of(config.getConfigKey(), param.getConfigValue()));
    }

    @Override
    @Transactional
    public void update(SysConfigEditParam param) {
        // 按主键加载
        SysConfig config = this.getById(param.getId());
        if (config == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Config not found");
        }
        SysConfig existing = getBaseMapper().selectOne(Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, param.getConfigKey()).last("limit 1"));
        if (existing != null && !config.getId().equals(existing.getId())) {
            throw new BizException("Config key already exists");
        }
        if (Boolean.TRUE.equals(config.getIsBuiltin())
                && StringUtils.hasText(param.getScene())
                && !param.getScene().equals(config.getScene())) {
            throw new BizException("内置配置不可修改场景编码");
        }
        if (Boolean.TRUE.equals(config.getIsBuiltin())) {
            param.setIsBuiltin(true);
            param.setScene(config.getScene());
            if (!StringUtils.hasText(param.getScope())) {
                param.setScope(config.getScope());
            }
        }
        AuditSnapshots.before(config);
        configConvert.update(param, config);
        if (!StringUtils.hasText(config.getValueType())) {
            config.setValueType("STRING");
        }
        config.setConfigValue(encryptValue(config.getConfigKey(), config.getConfigValue()));
        this.updateById(config);
        AuditSnapshots.after(config);
        afterMutation("update", patchPlainValue(config.getConfigKey(), param.getConfigValue()));
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysConfig> deleted = new ArrayList<>();
        for (List<String> batch : BatchPartition.partition(ids)) {
            List<SysConfig> configs = getBaseMapper().selectByIds(batch);
            List<String> builtinKeys = configs.stream()
                    .filter(item -> Boolean.TRUE.equals(item.getIsBuiltin()))
                    .map(SysConfig::getConfigKey)
                    .collect(Collectors.toList());
            if (!builtinKeys.isEmpty()) {
                throw new BizException("内置配置不可删除: " + String.join(", ", builtinKeys));
            }
            AuditSnapshots.deletedAll(configs);
            deleted.addAll(configs);
            this.removeByIds(batch);
        }
        Map<String, String> patches = new HashMap<>();
        for (SysConfig config : deleted) {
            patches.put(config.getConfigKey(), null);
        }
        afterMutation("delete", patches);
    }

    @Override
    @ReadDataSource
    public SysConfigResult detail(String id) {
        // 按主键加载
        SysConfig config = this.getById(id);
        if (config == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Config not found");
        }
        return toPublicResult(config);
    }

    @Override
    @ReadDataSource
    public Page<SysConfigResult> page(SysConfigPageParam param) {
        // 分页查询
        Page<SysConfig> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysConfig>lambdaQuery()
                        .like(StringUtils.hasText(param.getConfigKey()), SysConfig::getConfigKey, param.getConfigKey())
                        .eq(StringUtils.hasText(param.getCategory()), SysConfig::getCategory, param.getCategory())
                        .orderByAsc(SysConfig::getSortCode)
                        .orderByDesc(SysConfig::getCreatedAt));
        Page<SysConfigResult> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toPublicResults(page.getRecords()));
        return result;
    }

    @Override
    @ReadDataSource
    public List<SysConfigResult> list(String category) {
        // 组装查询条件
        return toPublicResults(getBaseMapper().selectList(Wrappers.<SysConfig>lambdaQuery()
                .eq(StringUtils.hasText(category), SysConfig::getCategory, category)
                .orderByAsc(SysConfig::getSortCode)));
    }

    @Override
    @Transactional
    public void batchSave(SysConfigBatchSaveParam param) {
        // 过滤敏感空值项
        List<SysConfigBatchItemParam> itemsToSave = new ArrayList<>();
        for (SysConfigBatchItemParam item : param.getItems()) {
            if (isSensitive(item.getConfigKey()) && !StringUtils.hasText(item.getConfigValue())) {
                continue;
            }
            itemsToSave.add(item);
        }
        if (itemsToSave.isEmpty()) {
            return;
        }
        Set<String> keys = new HashSet<>();
        for (SysConfigBatchItemParam item : itemsToSave) {
            keys.add(item.getConfigKey());
        }
        // 分批加载已有配置
        Map<String, SysConfig> existing = new HashMap<>();
        for (List<String> batch : BatchPartition.partition(new ArrayList<>(keys))) {
            getBaseMapper().selectList(Wrappers.<SysConfig>lambdaQuery().in(SysConfig::getConfigKey, batch))
                    .forEach(row -> existing.put(row.getConfigKey(), row));
        }
        // 拆分新建与更新并加密
        List<SysConfig> toCreate = new ArrayList<>();
        List<SysConfig> toUpdate = new ArrayList<>();
        for (SysConfigBatchItemParam item : itemsToSave) {
            SysConfig config = existing.get(item.getConfigKey());
            if (config == null) {
                SysConfig created = new SysConfig();
                created.setConfigKey(item.getConfigKey());
                created.setConfigValue(encryptValue(item.getConfigKey(), item.getConfigValue()));
                created.setCategory(item.getCategory());
                created.setRemark(item.getRemark());
                created.setValueType(StringUtils.hasText(item.getValueType()) ? item.getValueType() : "STRING");
                created.setLabel(item.getLabel());
                created.setScope(item.getScope());
                created.setScene(item.getScene());
                created.setIsBuiltin(item.getIsBuiltin() != null && item.getIsBuiltin());
                created.setExtJson(Map.of());
                created.setSortCode(0);
                toCreate.add(created);
                continue;
            }
            AuditSnapshots.before(config);
            config.setConfigValue(encryptValue(item.getConfigKey(), item.getConfigValue()));
            if (item.getCategory() != null) {
                config.setCategory(item.getCategory());
            }
            if (item.getRemark() != null) {
                config.setRemark(item.getRemark());
            }
            if (item.getValueType() != null) {
                config.setValueType(item.getValueType());
            }
            if (item.getLabel() != null) {
                config.setLabel(item.getLabel());
            }
            if (item.getScope() != null) {
                config.setScope(item.getScope());
            }
            if (item.getScene() != null) {
                config.setScene(item.getScene());
            }
            if (item.getIsBuiltin() != null) {
                config.setIsBuiltin(item.getIsBuiltin());
            }
            toUpdate.add(config);
            AuditSnapshots.after(config);
        }
        // 分批落库后刷新缓存
        int size = BatchPartition.DEFAULT_SIZE;
        for (int i = 0; i < toCreate.size(); i += size) {
            List<SysConfig> batch = toCreate.subList(i, Math.min(i + size, toCreate.size()));
            this.saveBatch(batch);
            batch.forEach(AuditSnapshots::created);
        }
        for (int i = 0; i < toUpdate.size(); i += size) {
            this.updateBatchById(toUpdate.subList(i, Math.min(i + size, toUpdate.size())));
        }
        Map<String, String> patches = new HashMap<>();
        for (SysConfigBatchItemParam item : itemsToSave) {
            patches.put(item.getConfigKey(), item.getConfigValue());
        }
        afterMutation("batchSave", patches);
    }

    @Override
    public String getValue(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        String cached = valueCache.get(key);
        if (cached != null) {
            return cached;
        }
        SysConfig config = getBaseMapper().selectOne(Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, key).last("limit 1"));
        if (config == null || config.getConfigValue() == null) {
            return null;
        }
        String plain = decryptValue(config.getConfigValue());
        valueCache.put(key, plain);
        return plain;
    }

    @Override
    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(normalized) || "1".equals(normalized) || "yes".equals(normalized) || "on".equals(normalized)) {
            return true;
        }
        if ("false".equals(normalized) || "0".equals(normalized) || "no".equals(normalized) || "off".equals(normalized)) {
            return false;
        }
        return defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        String value = getValue(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public Map<String, String> snapshot() {
        return new HashMap<>(valueCache);
    }

    @Override
    public void invalidateLocalCache() {
        warmCache();
    }

    private static boolean isSensitive(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return false;
        }
        if (SENSITIVE_CONFIG_KEYS.contains(configKey)) {
            return true;
        }
        return configKey.startsWith("AUTH_OAUTH_")
                && (configKey.endsWith("_CLIENT_SECRET") || configKey.endsWith("_APP_SECRET"));
    }

    private Map<String, String> patchPlainValue(String configKey, String plainValue) {
        if (isSensitive(configKey) && !StringUtils.hasText(plainValue)) {
            return Map.of();
        }
        return Map.of(configKey, plainValue);
    }

    private void afterMutation(String reason, Map<String, String> patches) {
        applyRuntimePatches(patches);
        Runnable postCommit = () -> {
            warmCache();
            configChangeNotifier.publish(reason);
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    postCommit.run();
                }
            });
            return;
        }
        postCommit.run();
    }

    private void applyRuntimePatches(Map<String, String> patches) {
        if (patches == null || patches.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : patches.entrySet()) {
            if (entry.getValue() == null) {
                valueCache.remove(entry.getKey());
            } else {
                valueCache.put(entry.getKey(), entry.getValue());
            }
        }
        RuntimeSettingsHolder.reload();
        configChangeNotifier.runAfterReloadHandlers();
    }

    private void warmCache() {
        List<SysConfig> all = getBaseMapper().selectList(Wrappers.lambdaQuery());
        ConcurrentHashMap<String, String> nextCache = new ConcurrentHashMap<>();
        for (SysConfig config : all) {
            if (StringUtils.hasText(config.getConfigKey()) && config.getConfigValue() != null) {
                nextCache.put(config.getConfigKey(), decryptValue(config.getConfigValue()));
            }
        }
        valueCache = nextCache;
        RuntimeSettingsHolder.reload();
        configChangeNotifier.runAfterReloadHandlers();
    }

    private String decryptValue(String raw) {
        return configCryptoService.decryptForRead(raw);
    }

    private String encryptValue(String configKey, String raw) {
        // 敏感值加密
        return configCryptoService.encryptForWrite(configKey, raw, isSensitive(configKey));
    }

    private SysConfigResult toPublicResult(SysConfig config) {
        SysConfigResult result = configConvert.toResult(config);
        if (result == null) {
            return null;
        }
        String raw = result.getConfigValue();
        if (raw == null) {
            return result;
        }
        String plain = decryptValue(raw);
        if (isSensitive(result.getConfigKey()) && StringUtils.hasText(plain)) {
            result.setConfigValue("");
            Map<String, Object> ext = result.getExtJson() != null
                    ? new HashMap<>(result.getExtJson())
                    : new HashMap<>();
            ext.put("is_set", true);
            result.setExtJson(ext);
            return result;
        }
        result.setConfigValue(plain);
        return result;
    }

    private List<SysConfigResult> toPublicResults(List<SysConfig> configs) {
        List<SysConfigResult> results = new ArrayList<>(configs.size());
        for (SysConfig config : configs) {
            results.add(toPublicResult(config));
        }
        return results;
    }
}
