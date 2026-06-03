package log

import (
	"bytes"
	"encoding/json"
	"io"
	"strings"

	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// ParseUserAgent extracts browser and OS from a User-Agent string.
// Delegates to utils.GetBrowser and utils.GetOS.
func ParseUserAgent(ua string) (browser, os string) {
	if ua == "" {
		return "-", "-"
	}
	return utils.GetBrowser(ua), utils.GetOS(ua)
}

// ExtractParamsJson extracts POST/PUT/PATCH body params as JSON,
// excluding infrastructure params (request, db, file). For GET/DELETE returns "".
func ExtractParamsJson(c *gin.Context) string {
	if c.Request.Method != "POST" && c.Request.Method != "PUT" && c.Request.Method != "PATCH" {
		return ""
	}

	bodyBytes, err := io.ReadAll(c.Request.Body)
	if err != nil {
		return ""
	}
	// Restore body for downstream handlers (e.g. ShouldBindJSON)
	c.Request.Body = io.NopCloser(bytes.NewBuffer(bodyBytes))

	var params map[string]any
	if err := json.Unmarshal(bodyBytes, &params); err != nil {
		return ""
	}

	excluded := map[string]bool{"request": true, "db": true, "file": true}
	filtered := make(map[string]any)
	for k, v := range params {
		if excluded[k] || v == nil {
			continue
		}
		filtered[k] = v
	}

	if len(filtered) == 0 {
		return ""
	}

	data, err := json.Marshal(filtered)
	if err != nil {
		return ""
	}
	return string(data)
}

// GetResultJson serializes a result value to JSON string.
// Returns "" if result is nil or serialization fails.
func GetResultJson(result any) string {
	if result == nil {
		return ""
	}
	data, err := json.Marshal(result)
	if err != nil {
		return ""
	}
	return string(data)
}

// GenerateLogSignature generates an SM3-based signature for log tamper-proofing.
// Uses the salt "hei-log-sign" matching the Python implementation.
func GenerateLogSignature(opData map[string]any) string {
	buf := new(bytes.Buffer)
	enc := json.NewEncoder(buf)
	enc.SetEscapeHTML(false)
	if err := enc.Encode(opData); err != nil {
		return ""
	}
	content := strings.TrimRight(buf.String(), "\n")
	return utils.HashWithSalt(content, "hei-log-sign")
}
