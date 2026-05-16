package utils

import (
	"strings"

	"github.com/google/uuid"
)

// TraceIDHeader is the HTTP header name for trace ID propagation.
// Must match fastapi's TRACE_ID_HEADER for inter-service compatibility.
const TraceIDHeader = "trace_id"

// GenerateTraceID creates a new trace ID (UUID without dashes, matching fastapi's uuid4().hex).
func GenerateTraceID() string {
	return strings.ReplaceAll(uuid.New().String(), "-", "")
}
