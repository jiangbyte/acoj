package storage

import (
	"bytes"
	"context"
	"errors"
	"io"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

// MinioStorage implements FileStorage using MinIO (S3-compatible object storage).
type MinioStorage struct {
	client        *minio.Client
	defaultBucket string
	endpoint      string
}

// NewMinioStorage creates a new MinIO storage backend.
func NewMinioStorage(endpoint, accessKey, secretKey, defaultBucket string, secure bool, region string) *MinioStorage {
	client, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKey, secretKey, ""),
		Secure: secure,
		Region: region,
	})
	if err != nil {
		panic(err)
	}
	return &MinioStorage{
		client:        client,
		defaultBucket: defaultBucket,
		endpoint:      endpoint,
	}
}

// GetDefaultBucket returns the default bucket name.
func (m *MinioStorage) GetDefaultBucket() string {
	return m.defaultBucket
}

// _ensureBucket creates the bucket if it does not already exist.
func (m *MinioStorage) _ensureBucket(ctx context.Context, bucket string) error {
	exists, err := m.client.BucketExists(ctx, bucket)
	if err != nil {
		return err
	}
	if !exists {
		return m.client.MakeBucket(ctx, bucket, minio.MakeBucketOptions{})
	}
	return nil
}

// Store uploads raw bytes to MinIO and returns the object key.
func (m *MinioStorage) Store(bucket, fileKey string, data []byte) (string, error) {
	ctx := context.Background()
	if err := m._ensureBucket(ctx, bucket); err != nil {
		return "", err
	}
	_, err := m.client.PutObject(ctx, bucket, fileKey, bytes.NewReader(data), int64(len(data)), minio.PutObjectOptions{})
	if err != nil {
		return "", err
	}
	return bucket + "/" + fileKey, nil
}

// StoreStream reads all data from the reader and uploads it to MinIO.
func (m *MinioStorage) StoreStream(bucket, fileKey string, reader io.Reader) (string, error) {
	ctx := context.Background()
	if err := m._ensureBucket(ctx, bucket); err != nil {
		return "", err
	}
	data, err := io.ReadAll(reader)
	if err != nil {
		return "", err
	}
	_, err = m.client.PutObject(ctx, bucket, fileKey, bytes.NewReader(data), int64(len(data)), minio.PutObjectOptions{})
	if err != nil {
		return "", err
	}
	return bucket + "/" + fileKey, nil
}

// GetBytes downloads and returns the raw bytes of an object from MinIO.
func (m *MinioStorage) GetBytes(bucket, fileKey string) ([]byte, error) {
	ctx := context.Background()
	obj, err := m.client.GetObject(ctx, bucket, fileKey, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	defer obj.Close()
	return io.ReadAll(obj)
}

// GetURL returns the public endpoint-based URL for the object.
func (m *MinioStorage) GetURL(bucket, fileKey string) string {
	return m.endpoint + "/" + bucket + "/" + fileKey
}

// GetAuthURL returns a time-limited presigned URL for the object.
func (m *MinioStorage) GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error) {
	ctx := context.Background()
	expiry := time.Duration(timeoutMs) * time.Millisecond
	u, err := m.client.PresignedGetObject(ctx, bucket, fileKey, expiry, nil)
	if err != nil {
		return "", err
	}
	return u.String(), nil
}

// Delete removes an object from MinIO.
// Ignores service-level errors (e.g., object not found) for idempotent deletion.
func (m *MinioStorage) Delete(bucket, fileKey string) error {
	ctx := context.Background()
	err := m.client.RemoveObject(ctx, bucket, fileKey, minio.RemoveObjectOptions{})
	if err != nil {
		var errResp minio.ErrorResponse
		if errors.As(err, &errResp) {
			return nil
		}
		return err
	}
	return nil
}

// Exists checks whether an object exists in MinIO.
func (m *MinioStorage) Exists(bucket, fileKey string) (bool, error) {
	ctx := context.Background()
	_, err := m.client.StatObject(ctx, bucket, fileKey, minio.StatObjectOptions{})
	if err != nil {
		var errResp minio.ErrorResponse
		if errors.As(err, &errResp) {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

// Copy copies an object from source to destination within MinIO.
func (m *MinioStorage) Copy(srcBucket, srcKey, dstBucket, dstKey string) error {
	ctx := context.Background()
	src := minio.CopySrcOptions{
		Bucket: srcBucket,
		Object: srcKey,
	}
	dst := minio.CopyDestOptions{
		Bucket: dstBucket,
		Object: dstKey,
	}
	_, err := m.client.CopyObject(ctx, dst, src)
	return err
}
