package utility

import (
	"context"
	"encoding/hex"

	"github.com/gogf/gf/v2/net/ghttp"
	"github.com/google/uuid"
)

// Context key type for trace ID stored in request context.
type TraceContextKey string

const (
	// ContextKeyTraceID is the key used to store trace_id in request context.
	ContextKeyTraceID TraceContextKey = "trace_id"
	// TraceIDHeader is the HTTP header name for trace ID propagation.
	TraceIDHeader string = "X-Request-Id"
)

// GenerateTraceID creates a new UUID-based trace ID (without dashes).
func GenerateTraceID() string {
	u := uuid.New()
	return hex.EncodeToString(u[:])
}

// GetTraceID extracts the trace ID from context, or returns empty string.
func GetTraceID(ctx context.Context) string {
	if ctx == nil {
		return ""
	}
	if v := ctx.Value(ContextKeyTraceID); v != nil {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return ""
}

// MiddlewareTrace is a GoFrame middleware that generates/sets trace_id per request.
// It reads the X-Request-Id header from the incoming request, or generates a new UUID.
// The trace ID is stored in the request context and set on the response header.
func MiddlewareTrace(r *ghttp.Request) {
	traceId := r.GetHeader(TraceIDHeader)
	if traceId == "" {
		traceId = GenerateTraceID()
	}
	r.SetCtx(context.WithValue(r.Context(), ContextKeyTraceID, traceId))
	r.Response.Header().Set(TraceIDHeader, traceId)
	r.Middleware.Next()
}
