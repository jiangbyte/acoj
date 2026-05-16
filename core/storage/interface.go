package storage

import "io"

// FileStorageInterface defines the contract for file storage backends.
// Follows the same abstraction as hei-fastapi's storage interface.
type FileStorageInterface interface {
	// GetDefaultBucket returns the default bucket/namespace name.
	GetDefaultBucket() string

	// Store stores raw bytes under the given key in the specified bucket.
	Store(bucket, fileKey string, data []byte) (string, error)

	// StoreStream stores data from a reader under the given key.
	StoreStream(bucket, fileKey string, reader io.Reader) (string, error)

	// GetBytes retrieves the raw bytes for a given key.
	GetBytes(bucket, fileKey string) ([]byte, error)

	// GetURL returns the direct access URL for the given key.
	GetURL(bucket, fileKey string) (string, error)

	// GetAuthURL returns a time-limited authenticated URL.
	GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error)

	// Delete removes the object at the given key.
	Delete(bucket, fileKey string) error

	// Exists checks if an object exists at the given key.
	Exists(bucket, fileKey string) (bool, error)

	// Copy copies an object from source to destination.
	Copy(srcBucket, srcKey, dstBucket, dstKey string) error
}
