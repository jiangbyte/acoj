package storage

import (
	"bytes"
	"context"
	"io"
	"time"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

// MinioStorage implements FileStorageInterface using MinIO (S3-compatible object storage).
type MinioStorage struct {
	client        *minio.Client
	defaultBucket string
}

// NewMinioStorage creates a new MinIO storage backend.
func NewMinioStorage(endpoint, accessKey, secretKey, defaultBucket string, secure bool) (*MinioStorage, error) {
	client, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKey, secretKey, ""),
		Secure: secure,
	})
	if err != nil {
		return nil, err
	}
	return &MinioStorage{
		client:        client,
		defaultBucket: defaultBucket,
	}, nil
}

func (s *MinioStorage) GetDefaultBucket() string {
	return s.defaultBucket
}

func (s *MinioStorage) ensureBucket(ctx context.Context, bucket string) error {
	exists, err := s.client.BucketExists(ctx, bucket)
	if err != nil {
		return err
	}
	if !exists {
		return s.client.MakeBucket(ctx, bucket, minio.MakeBucketOptions{})
	}
	return nil
}

func (s *MinioStorage) Store(bucket, fileKey string, data []byte) (string, error) {
	ctx := context.Background()
	if err := s.ensureBucket(ctx, bucket); err != nil {
		return "", err
	}
	_, err := s.client.PutObject(ctx, bucket, fileKey, bytes.NewReader(data), int64(len(data)), minio.PutObjectOptions{})
	if err != nil {
		return "", err
	}
	return bucket + "/" + fileKey, nil
}

func (s *MinioStorage) StoreStream(bucket, fileKey string, reader io.Reader) (string, error) {
	ctx := context.Background()
	if err := s.ensureBucket(ctx, bucket); err != nil {
		return "", err
	}
	data, err := io.ReadAll(reader)
	if err != nil {
		return "", err
	}
	_, err = s.client.PutObject(ctx, bucket, fileKey, bytes.NewReader(data), int64(len(data)), minio.PutObjectOptions{})
	if err != nil {
		return "", err
	}
	return bucket + "/" + fileKey, nil
}

func (s *MinioStorage) GetBytes(bucket, fileKey string) ([]byte, error) {
	ctx := context.Background()
	obj, err := s.client.GetObject(ctx, bucket, fileKey, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	defer obj.Close()
	return io.ReadAll(obj)
}

func (s *MinioStorage) GetURL(bucket, fileKey string) (string, error) {
	return s.client.EndpointURL().String() + "/" + bucket + "/" + fileKey, nil
}

func (s *MinioStorage) GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error) {
	ctx := context.Background()
	expiry := time.Duration(timeoutMs) * time.Millisecond
	url, err := s.client.PresignedGetObject(ctx, bucket, fileKey, expiry, nil)
	if err != nil {
		return "", err
	}
	return url.String(), nil
}

func (s *MinioStorage) Delete(bucket, fileKey string) error {
	ctx := context.Background()
	return s.client.RemoveObject(ctx, bucket, fileKey, minio.RemoveObjectOptions{})
}

func (s *MinioStorage) Exists(bucket, fileKey string) (bool, error) {
	ctx := context.Background()
	_, err := s.client.StatObject(ctx, bucket, fileKey, minio.StatObjectOptions{})
	if err != nil {
		errResponse := minio.ToErrorResponse(err)
		if errResponse.Code == "NoSuchKey" {
			return false, nil
		}
		return false, err
	}
	return true, nil
}

func (s *MinioStorage) Copy(srcBucket, srcKey, dstBucket, dstKey string) error {
	ctx := context.Background()
	src := minio.CopySrcOptions{
		Bucket: srcBucket,
		Object: srcKey,
	}
	dst := minio.CopyDestOptions{
		Bucket: dstBucket,
		Object: dstKey,
	}
	_, err := s.client.CopyObject(ctx, dst, src)
	return err
}
