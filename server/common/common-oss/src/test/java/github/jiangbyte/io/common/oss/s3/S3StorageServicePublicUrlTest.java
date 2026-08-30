package github.jiangbyte.io.common.oss.s3;

/**
 * Author: Charlie
 **/

import github.jiangbyte.io.common.oss.config.OssProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServicePublicUrlTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Test
    void publicBucketUsesBaseUrlJoin() {
        OssProperties properties = new OssProperties();
        properties.getS3().setBucketPublic(true);
        properties.getS3().setBucket("vms");
        properties.getS3().setEndpoint("http://127.0.0.1:9000");
        properties.getS3().setPathStyleAccess(true);
        properties.getS3().setPublicBaseUrl("https://cdn.example.com/files");

        S3StorageService service = new S3StorageService(s3Client, s3Presigner, properties);
        assertEquals("https://cdn.example.com/files/uploads/a.png", service.publicUrl("uploads/a.png"));
    }

    @Test
    void publicBucketWithoutBaseUsesEndpointPathStyle() {
        OssProperties properties = new OssProperties();
        properties.getS3().setBucketPublic(true);
        properties.getS3().setBucket("vms");
        properties.getS3().setEndpoint("http://127.0.0.1:9000");
        properties.getS3().setPathStyleAccess(true);

        S3StorageService service = new S3StorageService(s3Client, s3Presigner, properties);
        assertEquals("http://127.0.0.1:9000/vms/uploads/a.png", service.publicUrl("uploads/a.png"));
    }

    @Test
    void privateBucketDoesNotRewriteHost() throws Exception {
        OssProperties properties = new OssProperties();
        properties.getS3().setBucketPublic(false);
        properties.getS3().setBucket("vms");
        properties.getS3().setEndpoint("http://127.0.0.1:9000");
        properties.getS3().setPathStyleAccess(true);
        properties.getS3().setPublicBaseUrl("https://cdn.example.com");
        properties.getS3().setPresignExpireSeconds(3600);

        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(URI.create(
                "http://127.0.0.1:9000/vms/uploads/a.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Signature=abc")
                .toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        S3StorageService service = new S3StorageService(s3Client, s3Presigner, properties);
        String url = service.publicUrl("uploads/a.png");
        assertFalse(url.startsWith("https://cdn.example.com"));
        assertTrue(url.contains("127.0.0.1:9000"));
        assertTrue(url.contains("X-Amz-Signature"));
    }
}
