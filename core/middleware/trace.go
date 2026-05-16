package middleware

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/utils"
)

func Trace() gin.HandlerFunc {
	return func(c *gin.Context) {
		traceID := c.GetHeader(utils.TraceIDHeader)
		if traceID == "" {
			traceID = utils.GenerateTraceID()
		}
		c.Set("trace_id", traceID)
		c.Header(utils.TraceIDHeader, traceID)
		c.Next()
	}
}
