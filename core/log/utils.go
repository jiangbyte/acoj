package log

import (
	"bytes"
	"encoding/json"
	"sort"
	"strings"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/utils"
)

// ParseUserAgent extracts browser and OS from a User-Agent string.
func ParseUserAgent(ua string) (browser, os string) {
	ua = strings.ToLower(ua)
	switch {
	case strings.Contains(ua, "edg"):
		browser = "Edge"
	case strings.Contains(ua, "chrome"):
		browser = "Chrome"
	case strings.Contains(ua, "safari"):
		browser = "Safari"
	case strings.Contains(ua, "firefox"):
		browser = "Firefox"
	case strings.Contains(ua, "opera") || strings.Contains(ua, "opr"):
		browser = "Opera"
	case strings.Contains(ua, "msie") || strings.Contains(ua, "trident"):
		browser = "IE"
	default:
		browser = "-"
	}

	switch {
	case strings.Contains(ua, "windows"):
		os = "Windows"
	case strings.Contains(ua, "mac os") || strings.Contains(ua, "macintosh"):
		os = "macOS"
	case strings.Contains(ua, "linux"):
		os = "Linux"
	case strings.Contains(ua, "android"):
		os = "Android"
	case strings.Contains(ua, "iphone") || strings.Contains(ua, "ipad"):
		os = "iOS"
	default:
		os = "-"
	}
	return
}

// GenerateLogSignature creates a salted hash signature for log tamper-proofing,
// matching fastapi's generate_log_signature which uses sm3 hash_with_salt.
// Builds JSON with sorted keys (matching Python's sort_keys=True) for deterministic output.
func GenerateLogSignature(opData map[string]interface{}) string {
	keys := make([]string, 0, len(opData))
	for k := range opData {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	var buf bytes.Buffer
	buf.WriteByte('{')
	for i, k := range keys {
		if i > 0 {
			buf.WriteByte(',')
		}
		keyJSON, _ := json.Marshal(k)
		buf.Write(keyJSON)
		buf.WriteByte(':')
		valJSON, _ := json.Marshal(opData[k])
		buf.Write(valJSON)
	}
	buf.WriteByte('}')

	return utils.HashWithSalt(buf.String(), "hei-log-sign")
}

// ExtractParamsJSON serializes request params to JSON, excluding gin context keys.
func ExtractParamsJSON(c *gin.Context) string {
	// Try reading body first
	bodyBytes, _ := c.Get("_request_body")
	if bodyStr, ok := bodyBytes.(string); ok && bodyStr != "" {
		return bodyStr
	}
	return "{}"
}

// GetResultJSON serializes the response body for logging purposes.
func GetResultJSON(c *gin.Context) string {
	respBody, _ := c.Get("_response_body")
	if bodyStr, ok := respBody.(string); ok {
		return bodyStr
	}
	return "{}"
}

// TimeTrack calculates the elapsed time since start.
func TimeTrack(start time.Time) int {
	return int(time.Since(start).Milliseconds())
}
