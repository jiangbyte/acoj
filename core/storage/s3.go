package storage

import (
	"bytes"
	"context"
	"io"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
)

// S3Storage implements FileStorage using AWS S3 or any S3-compatible object store.
type S3Storage struct {
	client        *s3.Client
	defaultBucket string
	endpoint      string
}

// NewS3Storage creates a new S3-compatible storage backend.
func NewS3Storage(endpoint, accessKey, secretKey, defaultBucket, region string, pathStyle bool) *S3Storage {
	cfg, err := config.LoadDefaultConfig(context.Background(),
		config.WithRegion(region),
		config.WithCredentialsProvider(credentials.NewStaticCredentialsProvider(accessKey, secretKey, "")),
	)
	if err != nil {
		panic(err)
	}

	client := s3.NewFromConfig(cfg, func(o *s3.Options) {
		o.BaseEndpoint = aws.String(endpoint)
		o.UsePathStyle = pathStyle
	})

	return &S3Storage{
		client:        client,
		defaultBucket: defaultBucket,
		endpoint:      endpoint,
	}
}

// GetDefaultBucket returns the default bucket name.
func (s *S3Storage) GetDefaultBucket() string {
	return s.defaultBucket
}

// _ensureBucket creates the bucket if it does not already exist.
func (s *S3Storage) _ensureBucket(ctx context.Context, bucket string) error {
	_, err := s.client.HeadBucket(ctx, &s3.HeadBucketInput{
		Bucket: aws.String(bucket),
	})
	if err != nil {
		_, err = s.client.CreateBucket(ctx, &s3.CreateBucketInput{
			Bucket: aws.String(bucket),
		})
		return err
	}
	return nil
}

// Store uploads raw bytes to S3 and returns the object key.
func (s *S3Storage) Store(bucket, fileKey string, data []byte) (string, error) {
	ctx := context.Background()
	if err := s._ensureBucket(ctx, bucket); err != nil {
		return "", err
	}
	_, err := s.client.PutObject(ctx, &s3.PutObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fileKey),
		Body:   bytes.NewReader(data),
	})
	if err != nil {
		return "", err
	}
	return bucket + "/" + fileKey, nil
}

// StoreStream reads all data from the reader and uploads it to S3.
func (s *S3Storage) StoreStream(bucket, fileKey string, reader io.Reader) (string, error) {
	ctx := context.Background()
	if err := s._ensureBucket(ctx, bucket); err != nil {
		return "", err
	}
	data, err := io.ReadAll(reader)
	if err != nil {
		return "", err
	}
	_, err = s.client.PutObject(ctx, &s3.PutObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fileKey),
		Body:   bytes.NewReader(data),
	})
	if err != nil {
		return "", err
	}
	return bucket + "/" + fileKey, nil
}

// GetBytes downloads and returns the raw bytes of an object from S3.
func (s *S3Storage) GetBytes(bucket, fileKey string) ([]byte, error) {
	ctx := context.Background()
	output, err := s.client.GetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fileKey),
	})
	if err != nil {
		return nil, err
	}
	defer output.Body.Close()
	return io.ReadAll(output.Body)
}

// GetURL returns the endpoint-based URL for the object.
func (s *S3Storage) GetURL(bucket, fileKey string) string {
	return s.endpoint + "/" + bucket + "/" + fileKey
}

// GetAuthURL returns a time-limited presigned URL for the object.
func (s *S3Storage) GetAuthURL(bucket, fileKey string, timeoutMs int) (string, error) {
	ctx := context.Background()
	presignClient := s3.NewPresignClient(s.client)
	req, err := presignClient.PresignGetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fileKey),
	}, func(opts *s3.PresignOptions) {
		opts.Expires = time.Duration(timeoutMs) * time.Millisecond
	})
	if err != nil {
		return "", err
	}
	return req.URL, nil
}

// Delete removes an object from S3.
func (s *S3Storage) Delete(bucket, fileKey string) error {
	ctx := context.Background()
	_, err := s.client.DeleteObject(ctx, &s3.DeleteObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fileKey),
	})
	return err
}

// Exists checks whether an object exists in S3.
func (s *S3Storage) Exists(bucket, fileKey string) (bool, error) {
	ctx := context.Background()
	_, err := s.client.HeadObject(ctx, &s3.HeadObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(fileKey),
	})
	if err != nil {
		return false, nil
	}
	return true, nil
}

// Copy copies an object from source to destination within S3.
func (s *S3Storage) Copy(srcBucket, srcKey, dstBucket, dstKey string) error {
	ctx := context.Background()
	_, err := s.client.CopyObject(ctx, &s3.CopyObjectInput{
		Bucket:     aws.String(dstBucket),
		CopySource: aws.String(srcBucket + "/" + srcKey),
		Key:        aws.String(dstKey),
	})
	return err
}
