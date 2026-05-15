package file

import (
	"bytes"
	"context"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"sort"
	"strings"
	"time"
)

// S3Storage implements StorageInterface for S3-compatible object storage
// (MinIO, AWS S3, Aliyun OSS, Tencent COS).
type S3Storage struct {
	client *s3Client
}

type s3Client struct {
	endpoint      string
	accessKey     string
	secretKey     string
	defaultBucket string
	region        string
	host          string
	pathStyle     bool
	httpClient    *http.Client
}

func newS3Client(endpoint, accessKey, secretKey, defaultBucket, region string, pathStyle bool) (*s3Client, error) {
	u, err := url.Parse(endpoint)
	if err != nil {
		return nil, fmt.Errorf("invalid S3 endpoint: %w", err)
	}
	host := u.Host
	if host == "" {
		host = u.Path
	}
	if region == "" {
		region = "us-east-1"
	}
	return &s3Client{
		endpoint:      strings.TrimRight(endpoint, "/"),
		accessKey:     accessKey,
		secretKey:     secretKey,
		defaultBucket: defaultBucket,
		region:        region,
		host:          host,
		pathStyle:     pathStyle,
		httpClient:    &http.Client{Timeout: 30 * time.Second},
	}, nil
}

// NewS3Storage creates a new S3Storage.
func NewS3Storage(endpoint, accessKey, secretKey, defaultBucket, region string, pathStyle bool) (*S3Storage, error) {
	client, err := newS3Client(endpoint, accessKey, secretKey, defaultBucket, region, pathStyle)
	if err != nil {
		return nil, err
	}
	return &S3Storage{client: client}, nil
}

func (s *S3Storage) GetDefaultBucket() string {
	return s.client.defaultBucket
}

func (s *S3Storage) Store(ctx context.Context, bucket, fileKey string, data []byte) error {
	return s.client.putObject(ctx, bucket, fileKey, data)
}

func (s *S3Storage) GetBytes(ctx context.Context, bucket, fileKey string) ([]byte, error) {
	return s.client.getObject(ctx, bucket, fileKey)
}

func (s *S3Storage) GetURL(bucket, fileKey string) string {
	return s.client.objectURL(bucket, fileKey)
}

func (s *S3Storage) Delete(ctx context.Context, bucket, fileKey string) error {
	return s.client.deleteObject(ctx, bucket, fileKey)
}

// ---------------------------------------------------------------------------
// S3 REST API helpers using AWS Signature V4
// ---------------------------------------------------------------------------

// buildS3RequestURL builds the request URL for an S3 object operation.
func (c *s3Client) buildS3RequestURL(bucket, fileKey string) string {
	key := url.PathEscape(fileKey)
	if c.pathStyle {
		return fmt.Sprintf("%s/%s/%s", c.endpoint, bucket, key)
	}
	return fmt.Sprintf("%s/%s", c.endpoint, key)
}

// objectURL returns the HTTP URL for an object.
func (c *s3Client) objectURL(bucket, fileKey string) string {
	return c.buildS3RequestURL(bucket, fileKey)
}

// putObject uploads data to S3-compatible storage using PUT.
func (c *s3Client) putObject(ctx context.Context, bucket, fileKey string, data []byte) error {
	requestURL := c.buildS3RequestURL(bucket, fileKey)
	headers, err := c.sign("PUT", requestURL, data)
	if err != nil {
		return err
	}

	req, err := http.NewRequestWithContext(ctx, "PUT", requestURL, bytes.NewReader(data))
	if err != nil {
		return fmt.Errorf("failed to create PUT request: %w", err)
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("S3 PUT failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 300 {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("S3 PUT returned %d: %s", resp.StatusCode, string(body))
	}
	return nil
}

// getObject downloads data from S3-compatible storage using GET.
func (c *s3Client) getObject(ctx context.Context, bucket, fileKey string) ([]byte, error) {
	requestURL := c.buildS3RequestURL(bucket, fileKey)
	headers, err := c.sign("GET", requestURL, nil)
	if err != nil {
		return nil, err
	}

	req, err := http.NewRequestWithContext(ctx, "GET", requestURL, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create GET request: %w", err)
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("S3 GET failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 300 {
		body, _ := io.ReadAll(resp.Body)
		return nil, fmt.Errorf("S3 GET returned %d: %s", resp.StatusCode, string(body))
	}

	return io.ReadAll(resp.Body)
}

// deleteObject removes an object from S3-compatible storage using DELETE.
func (c *s3Client) deleteObject(ctx context.Context, bucket, fileKey string) error {
	requestURL := c.buildS3RequestURL(bucket, fileKey)
	headers, err := c.sign("DELETE", requestURL, nil)
	if err != nil {
		return err
	}

	req, err := http.NewRequestWithContext(ctx, "DELETE", requestURL, nil)
	if err != nil {
		return fmt.Errorf("failed to create DELETE request: %w", err)
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("S3 DELETE failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 300 {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("S3 DELETE returned %d: %s", resp.StatusCode, string(body))
	}
	return nil
}

// ---------------------------------------------------------------------------
// AWS Signature V4 implementation
// ---------------------------------------------------------------------------

// sign calculates AWS Signature V4 headers for the given HTTP method, URL, and body.
func (c *s3Client) sign(method, requestURL string, body []byte) (map[string]string, error) {
	u, err := url.Parse(requestURL)
	if err != nil {
		return nil, fmt.Errorf("failed to parse request URL: %w", err)
	}

	now := time.Now().UTC()
	amzDate := now.Format("20060102T150405Z")
	dateStamp := now.Format("20060102")

	if body == nil {
		body = []byte{}
	}
	payloadHash := sha256Hex(body)

	canonicalURI := u.Path
	if canonicalURI == "" {
		canonicalURI = "/"
	}
	canonicalQueryString := u.RawQuery

	// Build canonical headers: host + x-amz-content-sha256 + x-amz-date
	headers := map[string]string{
		"host":                 c.host,
		"x-amz-content-sha256": payloadHash,
		"x-amz-date":           amzDate,
	}

	// Sort header names
	var headerNames []string
	for k := range headers {
		headerNames = append(headerNames, strings.ToLower(k))
	}
	sort.Strings(headerNames)

	// Build canonical headers string and signed headers string
	var canonicalHeadersBuilder strings.Builder
	for _, name := range headerNames {
		canonicalHeadersBuilder.WriteString(name)
		canonicalHeadersBuilder.WriteString(":")
		canonicalHeadersBuilder.WriteString(strings.TrimSpace(headers[name]))
		canonicalHeadersBuilder.WriteString("\n")
	}
	canonicalHeaders := canonicalHeadersBuilder.String()
	signedHeaders := strings.Join(headerNames, ";")

	// Canonical request
	canonicalRequest := strings.Join([]string{
		method,
		canonicalURI,
		canonicalQueryString,
		canonicalHeaders,
		signedHeaders,
		payloadHash,
	}, "\n")

	// Hash the canonical request
	canonicalRequestHash := sha256Hex([]byte(canonicalRequest))

	// Credential scope
	credentialScope := fmt.Sprintf("%s/%s/s3/aws4_request", dateStamp, c.region)

	// String to sign
	stringToSign := strings.Join([]string{
		"AWS4-HMAC-SHA256",
		amzDate,
		credentialScope,
		canonicalRequestHash,
	}, "\n")

	// Signing key
	signingKey := c.signingKey(dateStamp)

	// Signature
	signature := hmacSHA256Hex(signingKey, stringToSign)

	// Authorization header
	authHeader := fmt.Sprintf(
		"AWS4-HMAC-SHA256 Credential=%s/%s, SignedHeaders=%s, Signature=%s",
		c.accessKey, credentialScope, signedHeaders, signature,
	)

	result := make(map[string]string, len(headers)+1)
	for k, v := range headers {
		result[http.CanonicalHeaderKey(k)] = v
	}
	result["Authorization"] = authHeader

	return result, nil
}

// signingKey derives the AWS Signature V4 signing key.
func (c *s3Client) signingKey(dateStamp string) []byte {
	kDate := hmacSHA256([]byte("AWS4"+c.secretKey), dateStamp)
	kRegion := hmacSHA256(kDate, c.region)
	kService := hmacSHA256(kRegion, "s3")
	kSigning := hmacSHA256(kService, "aws4_request")
	return kSigning
}

// sha256Hex returns the hex-encoded SHA-256 hash of data.
func sha256Hex(data []byte) string {
	h := sha256.Sum256(data)
	return hex.EncodeToString(h[:])
}

// hmacSHA256 computes HMAC-SHA256 of data using the given key.
func hmacSHA256(key []byte, data string) []byte {
	h := hmac.New(sha256.New, key)
	h.Write([]byte(data))
	return h.Sum(nil)
}

// hmacSHA256Hex computes HMAC-SHA256 and returns the hex-encoded result.
func hmacSHA256Hex(key []byte, data string) string {
	return hex.EncodeToString(hmacSHA256(key, data))
}
