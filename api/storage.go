package api

type StorageAPI interface {
	Upload(bucket, objectName string, data []byte, contentType string) (string, error)
	Delete(bucket, objectName string) error
	GetURL(bucket, objectName string) string
}
