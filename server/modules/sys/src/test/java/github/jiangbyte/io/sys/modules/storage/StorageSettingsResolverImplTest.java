package github.jiangbyte.io.sys.modules.storage;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.sys.config.RuntimeSettings;
import github.jiangbyte.io.sys.config.RuntimeSettingsHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StorageSettingsResolverImplTest {

    private StorageSettingsResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new StorageSettingsResolverImpl();
        RuntimeSettingsHolder.bindLoader(() -> Map.of());
    }

    @AfterEach
    void tearDown() {
        RuntimeSettingsHolder.bindLoader(() -> Map.of());
    }

    @Test
    void usesRustfsDefaultsWhenKeysMissing() {
        bind(Map.of());

        ResolvedStorageConfig config = resolver.resolveDefault();

        assertEquals("rustfs", config.getProvider());
        assertEquals("RUSTFS", config.getEngine());
        assertFalse(config.isBucketPublic());
        assertEquals(10_485_760L, config.getUploadMaxBytes());
    }

    @Test
    void mapsMinioEngineToS3CompatibleProvider() {
        bind(Map.of(
                "DEFAULT_FILE_ENGINE", "MINIO",
                "STORAGE_MINIO_BUCKET", "bucket",
                "STORAGE_MINIO_ENDPOINT", "http://127.0.0.1:9000",
                "STORAGE_MINIO_ACCESS_KEY", "ak",
                "STORAGE_MINIO_SECRET_KEY", "sk",
                "STORAGE_MINIO_USE_SSL", "false",
                "STORAGE_MINIO_BUCKET_PUBLIC", "true",
                "STORAGE_MINIO_BASE_URL", "https://cdn.example.com"
        ));

        ResolvedStorageConfig config = resolver.resolveDefault();

        assertEquals("minio", config.getProvider());
        assertEquals("MINIO", config.getEngine());
        assertEquals("bucket", config.getBucket());
        assertTrue(config.isPathStyleAccess());
        assertTrue(config.isBucketPublic());
        assertEquals("https://cdn.example.com", config.getBaseUrl());
        assertEquals("s3", FileEngines.toOssType(config.getProvider()));
    }

    private static void bind(Map<String, String> values) {
        Map<String, String> copy = new HashMap<>(values);
        RuntimeSettingsHolder.bindLoader(() -> copy);
        RuntimeSettingsHolder.reload();
        RuntimeSettings ignored = RuntimeSettingsHolder.get();
        assertEquals(copy, ignored.asMap());
    }
}
