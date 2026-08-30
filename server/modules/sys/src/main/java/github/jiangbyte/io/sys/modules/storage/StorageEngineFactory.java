package github.jiangbyte.io.sys.modules.storage;

import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.oss.StorageService;
import github.jiangbyte.io.common.oss.s3.S3ClientFactory;
import github.jiangbyte.io.sys.config.RuntimeSettingsHolder;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 存储引擎工厂：按解析配置创建具体 StorageEngine。
 *
 * Author: Charlie
 */
@Component
public class StorageEngineFactory {

    private final StorageSettingsResolver storageSettingsResolver;
    private final ConcurrentMap<String, CachedEngine> engines = new ConcurrentHashMap<>();
    private volatile long boundVersion = -1L;

    public StorageEngineFactory(StorageSettingsResolver storageSettingsResolver) {
        this.storageSettingsResolver = storageSettingsResolver;
    }

    public StorageService getDefault() {
        return get(storageSettingsResolver.resolveDefault());
    }

    public StorageService get(String storageProvider) {
        return get(storageSettingsResolver.resolve(storageProvider));
    }

    public StorageService get(ResolvedStorageConfig config) {
        if (config == null) {
            throw new BizException(500, "Storage config is not available");
        }
        ensureVersion();
        String key = config.getProvider() == null ? "default" : config.getProvider();
        return engines.compute(key, (k, existing) -> {
            if (existing != null && existing.matches(config, boundVersion)) {
                return existing;
            }
            if (existing != null) {
                existing.closeQuietly();
            }
            return CachedEngine.create(config, boundVersion);
        }).service();
    }

    public void refresh() {
        boundVersion = -1L;
        engines.values().forEach(CachedEngine::closeQuietly);
        engines.clear();
    }

    @PreDestroy
    public void destroy() {
        refresh();
    }

    private void ensureVersion() {
        long version = RuntimeSettingsHolder.get().version();
        if (version != boundVersion) {
            synchronized (this) {
                if (version != boundVersion) {
                    engines.values().forEach(CachedEngine::closeQuietly);
                    engines.clear();
                    boundVersion = version;
                }
            }
        }
    }

    private record CachedEngine(ResolvedStorageConfig config, long version, StorageService service,
                                AutoCloseable closeable) {
        static CachedEngine create(ResolvedStorageConfig config, long version) {
            if (!config.isS3Compatible()) {
                throw new BizException(500, "Unsupported storage provider: " + config.getProvider());
            }
            S3ClientFactory.ManagedS3Storage managed = S3ClientFactory.create(config.toOssProperties());
            return new CachedEngine(config, version, managed, managed);
        }

        boolean matches(ResolvedStorageConfig other, long currentVersion) {
            return version == currentVersion && Objects.equals(config, other);
        }

        void closeQuietly() {
            if (closeable == null) {
                return;
            }
            try {
                closeable.close();
            } catch (Exception ignored) {
                // 忽略
            }
        }
    }
}
