package utils

import (
	"encoding/hex"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

// GenerateTraceID generates a new trace ID (UUID without dashes).
func GenerateTraceID() string {
	id := uuid.New()
	return hex.EncodeToString(id[:])
}

// GetTraceID returns the current trace ID from the Gin context.
func GetTraceID(c *gin.Context) string {
	return c.GetString("trace_id")
}
