package file

import "context"

// StorageInterface defines the interface for file storage backends.
type StorageInterface interface {
	GetDefaultBucket() string
	Store(ctx context.Context, bucket, fileKey string, data []byte) error
	GetBytes(ctx context.Context, bucket, fileKey string) ([]byte, error)
	GetURL(bucket, fileKey string) string
	Delete(ctx context.Context, bucket, fileKey string) error
}
