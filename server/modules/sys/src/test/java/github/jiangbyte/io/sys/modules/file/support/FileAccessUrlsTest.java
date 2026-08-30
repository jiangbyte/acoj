package github.jiangbyte.io.sys.modules.file.support;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.sys.modules.file.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileAccessUrlsTest {

    @Mock
    private ObjectProvider<FileService> fileServiceProvider;

    @Mock
    private FileService fileService;

    private FileAccessUrls urls;

    @BeforeEach
    void setUp() {
        urls = new FileAccessUrls(fileServiceProvider);
    }

    @Test
    void normalizeObjectNameKeepsExternalUrl() {
        assertEquals("https://cdn.example.com/a.png", urls.normalizeObjectName("https://cdn.example.com/a.png"));
    }

    @Test
    void normalizeObjectNameStripsLegacyProxyPrefix() {
        assertEquals("uploads/a.png", urls.normalizeObjectName("/api/v1/files/uploads/a.png"));
        assertEquals("uploads/a.png", urls.normalizeObjectName("api/v1/files/uploads/a.png"));
    }

    @Test
    void toObjectKeyStripsBucketPrefixForPathStyle() {
        assertEquals(
                "uploads/a.png",
                urls.toObjectKey("http://127.0.0.1:9000/vms/uploads/a.png?X-Amz-Signature=abc"));
    }

    @Test
    void looksLikePresignedUrl() {
        assertTrue(urls.looksLikePresignedUrl("https://x/y?X-Amz-Signature=1"));
        assertFalse(urls.looksLikePresignedUrl("https://cdn.example.com/a.png"));
    }

    @Test
    void resolveFileUrlDelegatesToFileService() {
        when(fileServiceProvider.getIfAvailable()).thenReturn(fileService);
        when(fileService.resolveAccessUrl("uploads/a.png")).thenReturn("https://cdn.example.com/uploads/a.png");

        assertEquals("https://cdn.example.com/uploads/a.png", urls.resolveFileUrl("uploads/a.png"));
        verify(fileService).resolveAccessUrl("uploads/a.png");
    }

    @Test
    void resolveFileUrlReturnsNullWhenServiceMissing() {
        when(fileServiceProvider.getIfAvailable()).thenReturn(null);
        assertNull(urls.resolveFileUrl("uploads/a.png"));
    }

    @Test
    void contentDispositionIncludesFilenameStar() {
        String header = ContentDispositions.attachment("中文.png");
        assertTrue(header.contains("filename*="));
        assertTrue(header.contains("UTF-8''"));
    }
}
