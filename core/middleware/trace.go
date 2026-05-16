package middleware

import (
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// Trace returns a Gin middleware that manages trace ID lifecycle per request.
//
// It reads a trace_id from the incoming request header. If the header is empty,
// a new trace ID is generated. The trace ID is stored in the Gin context under
// the key "trace_id" for downstream handlers and loggers to use.
func Trace() gin.HandlerFunc {
	return func(c *gin.Context) {
		traceID := c.GetHeader("trace_id")
		if traceID == "" {
			traceID = utils.GenerateTraceID()
		}
		c.Set("trace_id", traceID)

		c.Next()
	}
}
