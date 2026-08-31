package github.jiangbyte.io.sys.modules.file.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.core.util.BatchPartition;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.oss.StorageService;
import github.jiangbyte.io.common.satoken.utils.LoginHelper;
import github.jiangbyte.io.sys.config.RuntimeSettings;
import github.jiangbyte.io.sys.config.RuntimeSettingsHolder;
import github.jiangbyte.io.sys.modules.file.convert.SysFileConvert;
import github.jiangbyte.io.sys.modules.file.entity.SysFile;
import github.jiangbyte.io.sys.modules.file.mapper.SysFileMapper;
import github.jiangbyte.io.sys.modules.file.param.SysFileEditParam;
import github.jiangbyte.io.sys.modules.file.param.SysFilePageParam;
import github.jiangbyte.io.sys.modules.file.result.SysFileUrlResult;
import github.jiangbyte.io.sys.modules.file.service.FileService;
import github.jiangbyte.io.sys.modules.file.support.FileAccessUrls;
import github.jiangbyte.io.sys.modules.storage.ResolvedStorageConfig;
import github.jiangbyte.io.sys.modules.storage.StorageEngineFactory;
import github.jiangbyte.io.sys.modules.storage.StorageSettingsResolver;
import github.jiangbyte.io.sys.modules.storage.StorageSettingsResolverImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import cn.hutool.core.util.IdUtil;

/**
 * 文件服务实现：对接存储引擎与元数据持久化。
 *
 * Author: Charlie
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements FileService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final StorageSettingsResolver storageSettingsResolver;
    private final StorageEngineFactory storageEngineFactory;
    private final SysFileConvert fileConvert;
    private final FileAccessUrls fileAccessUrls;

    @Override
    @Transactional
    public SysFile upload(MultipartFile file, String storageProvider) {
        return doUpload(file, storageProvider);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 分批加载 → 先删存储对象 → 再删库记录
        for (List<String> batch : BatchPartition.partition(ids)) {
            List<SysFile> files = getBaseMapper().selectByIds(batch);
            for (SysFile file : files) {
                // object_name 可能存了 URL/路径形式，删除前统一转纯 key
                String objectKey = fileAccessUrls.toObjectKey(file.getObjectName());
                if (objectKey == null) {
                    continue;
                }
                // 存储删除失败不阻断元数据清理（残留/存储不可达时仍可删除库记录），仅记录警告
                try {
                    storageFor(file).delete(objectKey);
                } catch (Exception ex) {
                    log.warn("Failed to delete storage object, skip (id={}, object={}, provider={}): {}",
                            file.getId(), objectKey, file.getStorageProvider(), ex.getMessage());
                }
            }
            AuditSnapshots.deletedAll(files);
            this.removeByIds(batch);
        }
    }

    @Override
    @Transactional
    public void update(SysFileEditParam param) {
        // 按主键加载
        SysFile file = this.getById(param.getId());
        if (file == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "File not found");
        }
        AuditSnapshots.before(file);
        fileConvert.update(param, file);
        this.updateById(file);
        AuditSnapshots.after(file);
    }

    @Override
    @ReadDataSource
    public SysFile detail(String id) {
        // 按主键加载
        SysFile file = this.getById(id);
        if (file == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "File not found");
        }
        return withResolvedUrl(file);
    }

    @Override
    public void assertOwnedByCurrent(SysFile file) {
        if (file == null) {
            throw new BizException(404, "File not found");
        }
        String accountId = LoginHelper.requireUser().getAccountId();
        if (!StringUtils.hasText(file.getCreatedBy()) || !accountId.equals(file.getCreatedBy())) {
            throw new BizException(403, "无权访问该文件");
        }
    }

    @Override
    @ReadDataSource
    public List<SysFile> listByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<SysFile> result = new ArrayList<>();
        for (List<String> batch : BatchPartition.partition(ids)) {
            getBaseMapper().selectByIds(batch).forEach(file -> result.add(withResolvedUrl(file)));
        }
        return result;
    }

    @Override
    public Resource download(String id) {
        SysFile file = detail(id);
        String objectKey = fileAccessUrls.toObjectKey(file.getObjectName());
        if (objectKey == null) {
            throw new BizException(404, "File not found");
        }
        return storageFor(file).load(objectKey);
    }

    @Override
    @ReadDataSource
    public SysFileUrlResult url(String objectName) {
        SysFile file = getBaseMapper().selectOne(Wrappers.<SysFile>lambdaQuery()
                .eq(SysFile::getObjectName, objectName).last("limit 1"));
        String objectKey = fileAccessUrls.toObjectKey(objectName);
        if (objectKey == null) {
            throw new BizException(404, "File not found");
        }
        return SysFileUrlResult.of(objectKey, storageFor(file).publicUrl(objectKey), file);
    }

    @Override
    @ReadDataSource
    public SysFileUrlResult presignedUrl(String objectName) {
        SysFile file = getBaseMapper().selectOne(Wrappers.<SysFile>lambdaQuery()
                .eq(SysFile::getObjectName, objectName).last("limit 1"));
        int expireSeconds = RuntimeSettingsHolder.get().getInt(
                StorageSettingsResolverImpl.KEY_PRESIGN_EXPIRE_SECONDS,
                StorageSettingsResolverImpl.DEFAULT_PRESIGN_EXPIRE_SECONDS);
        Duration ttl = Duration.ofSeconds(Math.max(1, expireSeconds));
        String objectKey = fileAccessUrls.toObjectKey(objectName);
        if (objectKey == null) {
            throw new BizException(404, "File not found");
        }
        return SysFileUrlResult.of(objectKey, storageFor(file).presignedUrl(objectKey, ttl), file);
    }

    @Override
    @ReadDataSource
    public Page<SysFile> page(SysFilePageParam param) {
        Page<SysFile> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysFile>lambdaQuery()
                        .like(StringUtils.hasText(param.getOriginalName()), SysFile::getOriginalName, param.getOriginalName())
                        .like(StringUtils.hasText(param.getObjectName()), SysFile::getObjectName, param.getObjectName())
                        .eq(StringUtils.hasText(param.getStorageProvider()), SysFile::getStorageProvider, param.getStorageProvider())
                        .like(StringUtils.hasText(param.getContentType()), SysFile::getContentType, param.getContentType())
                        .orderByDesc(SysFile::getCreatedAt));
        page.getRecords().forEach(this::withResolvedUrl);
        return page;
    }

    @Override
    @Transactional
    public void deleteByObjectName(String objectName) {
        if (!StringUtils.hasText(objectName) || objectName.startsWith("http://") || objectName.startsWith("https://")) {
            return;
        }
        SysFile file = getBaseMapper().selectOne(Wrappers.<SysFile>lambdaQuery()
                .eq(SysFile::getObjectName, objectName).last("limit 1"));
        if (file == null) {
            return;
        }
        AuditSnapshots.deleted(file);
        String objectKey = fileAccessUrls.toObjectKey(objectName);
        if (objectKey != null) {
            try {
                storageFor(file).delete(objectKey);
            } catch (Exception ex) {
                log.warn("Failed to delete storage object, skip (object={}, provider={}): {}",
                        objectKey, file.getStorageProvider(), ex.getMessage());
            }
        }
        this.removeById(file.getId());
    }

    @Override
    @ReadDataSource
    public String resolveAccessUrl(String objectNameOrUrl) {
        if (!StringUtils.hasText(objectNameOrUrl)) {
            return null;
        }
        String raw = objectNameOrUrl.trim();
        if (fileAccessUrls.isExternalUrl(raw)) {
            // 永久外链（无签名参数）原样返回；预签/脏存储 URL 再解析
            if (!fileAccessUrls.looksLikePresignedUrl(raw)) {
                return raw;
            }
            String objectKey = fileAccessUrls.toObjectKey(raw);
            if (!StringUtils.hasText(objectKey)) {
                return null;
            }
            SysFile file = findByObjectName(objectKey);
            if (file == null) {
                int slash = objectKey.indexOf('/');
                if (slash > 0) {
                    String alt = objectKey.substring(slash + 1);
                    file = findByObjectName(alt);
                    if (file != null) {
                        objectKey = alt;
                    }
                }
            }
            return storageFor(file).publicUrl(objectKey);
        }
        String objectKey = fileAccessUrls.toObjectKey(raw);
        if (!StringUtils.hasText(objectKey)) {
            return null;
        }
        SysFile file = findByObjectName(objectKey);
        return storageFor(file).publicUrl(objectKey);
    }

    private SysFile findByObjectName(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return null;
        }
        return getBaseMapper().selectOne(Wrappers.<SysFile>lambdaQuery()
                .eq(SysFile::getObjectName, objectKey).last("limit 1"));
    }

    @Override
    @ReadDataSource
    public List<SysFile> listByObjectNames(Collection<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String raw : objectNames) {
            String name = fileAccessUrls.normalizeObjectName(raw);
            if (StringUtils.hasText(name) && !fileAccessUrls.isExternalUrl(name)) {
                normalized.add(name);
            }
        }
        if (normalized.isEmpty()) {
            return List.of();
        }
        List<SysFile> result = new ArrayList<>();
        for (List<String> batch : BatchPartition.partition(normalized)) {
            getBaseMapper().selectList(Wrappers.<SysFile>lambdaQuery()
                            .in(SysFile::getObjectName, batch))
                    .forEach(file -> result.add(withResolvedUrl(file)));
        }
        return result;
    }

    private SysFile doUpload(MultipartFile file, String storageProvider) {
        if (file == null || !StringUtils.hasText(file.getOriginalFilename())) {
            throw new BizException("File is required");
        }
        // 解析存储配置并校验上传
        ResolvedStorageConfig storageConfig = storageSettingsResolver.resolve(storageProvider);
        validateUpload(file, storageConfig);
        String originalName = safeOriginalName(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String objectName = buildObjectName(originalName, "uploads");
        try {
            String contentType = sanitizeContentType(file.getContentType());
            StorageService storage = storageEngineFactory.get(storageConfig);
            // 写入对象存储（put 返回值仅作即时提示；落库不存预签名）
            storage.put(objectName, file.getInputStream(), file.getSize(), contentType);
            // 落库元数据并解析访问 URL
            SysFile entity = new SysFile();
            entity.setObjectName(objectName);
            entity.setOriginalName(originalName);
            entity.setStorageProvider(storageConfig.getProvider());
            entity.setBucket(storageConfig.getBucket());
            entity.setContentType(contentType);
            entity.setSize(file.getSize());
            entity.setUrl(objectName);
            this.save(entity);
            AuditSnapshots.created(entity);
            return withResolvedUrl(entity);
        } catch (IOException exception) {
            throw new BizException(500, "Failed to read upload stream");
        }
    }

    private SysFile withResolvedUrl(SysFile file) {
        if (file == null || !StringUtils.hasText(file.getObjectName())) {
            return file;
        }
        String objectKey = fileAccessUrls.toObjectKey(file.getObjectName());
        if (objectKey == null) {
            return file;
        }
        String resolved = storageFor(file).publicUrl(objectKey);
        if (StringUtils.hasText(resolved)) {
            file.setUrl(resolved);
        }
        return file;
    }

    private void validateUpload(MultipartFile file, ResolvedStorageConfig storageConfig) {
        RuntimeSettings settings = RuntimeSettingsHolder.get();
        // 校验大小
        long maxBytes = storageConfig.getUploadMaxBytes() > 0
                ? storageConfig.getUploadMaxBytes()
                : StorageSettingsResolverImpl.DEFAULT_UPLOAD_MAX_BYTES;
        if (file.getSize() > maxBytes) {
            throw new BizException(400, "File exceeds max size");
        }
        // 校验扩展名黑白名单
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = extensionOf(originalName);
        List<String> denied = readJsonStringList(settings.get("STORAGE_UPLOAD_DENIED_EXTENSIONS", "[]"));
        if (!denied.isEmpty() && denied.stream().anyMatch(e -> normalizeExt(e).equals(ext))) {
            throw new BizException(400, "File extension not allowed: " + ext);
        }
        List<String> allowedExt = readJsonStringList(settings.get("STORAGE_UPLOAD_ALLOWED_EXTENSIONS", "[]"));
        if (!allowedExt.isEmpty() && allowedExt.stream().noneMatch(e -> normalizeExt(e).equals(ext))) {
            throw new BizException(400, "File extension not allowed: " + ext);
        }
        // 校验 Content-Type
        String contentType = file.getContentType();
        List<String> allowedTypes = readJsonStringList(settings.get("STORAGE_UPLOAD_ALLOWED_CONTENT_TYPES", "[]"));
        if (StringUtils.hasText(contentType)
                && !allowedTypes.isEmpty()
                && allowedTypes.stream().noneMatch(t -> matchesContentType(contentType, t))) {
            throw new BizException(400, "Content type not allowed: " + contentType);
        }
    }

    private StorageService storageFor(SysFile file) {
        if (file != null && StringUtils.hasText(file.getStorageProvider())) {
            return storageEngineFactory.get(file.getStorageProvider());
        }
        return storageEngineFactory.getDefault();
    }

    private static String normalizeExt(String ext) {
        if (ext == null) {
            return "";
        }
        String value = ext.trim().toLowerCase(Locale.ROOT);
        return value.startsWith(".") ? value : "." + value;
    }

    private static boolean matchesContentType(String contentType, String allowed) {
        if (!StringUtils.hasText(contentType) || !StringUtils.hasText(allowed)) {
            return false;
        }
        String normalizedType = contentType.trim();
        String normalizedAllowed = allowed.trim();
        if (normalizedAllowed.endsWith("/*")) {
            String prefix = normalizedAllowed.substring(0, normalizedAllowed.length() - 1);
            return normalizedType.regionMatches(true, 0, prefix, 0, prefix.length());
        }
        return normalizedAllowed.equalsIgnoreCase(normalizedType);
    }

    private static List<String> readJsonStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private static String extensionOf(String filename) {
        String safe = filename.replace("\\", "/");
        int slash = safe.lastIndexOf('/');
        if (slash >= 0) {
            safe = safe.substring(slash + 1);
        }
        int dot = safe.lastIndexOf('.');
        return dot >= 0 ? safe.substring(dot).toLowerCase(Locale.ROOT) : "";
    }

    private static String safeOriginalName(String filename) {
        String safe = filename.replace("\\", "/");
        int slash = safe.lastIndexOf('/');
        if (slash >= 0) {
            safe = safe.substring(slash + 1);
        }
        return safe.isBlank() ? "file" : safe;
    }

    private static String sanitizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "application/octet-stream";
        }
        return contentType.trim();
    }

    private String buildObjectName(String filename, String category) {
        RuntimeSettings settings = RuntimeSettingsHolder.get();
        int maxCategoryLen = Math.max(1, settings.getInt("STORAGE_UPLOAD_CATEGORY_MAX_LENGTH", 64));
        String safeCategory = StringUtils.hasText(category) ? category.trim() : "uploads";
        if (safeCategory.length() > maxCategoryLen) {
            throw new BizException(400, "Upload category exceeds max length");
        }
        String safeName = filename.replace("\\", "/");
        int slash = safeName.lastIndexOf('/');
        if (slash >= 0) {
            safeName = safeName.substring(slash + 1);
        }
        int dot = safeName.lastIndexOf('.');
        String suffix = dot >= 0 ? safeName.substring(dot).toLowerCase(Locale.ROOT) : "";
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return "%s/%04d/%02d/%02d/%s%s".formatted(
                safeCategory,
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                IdUtil.simpleUUID(),
                suffix);
    }
}