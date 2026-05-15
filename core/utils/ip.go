package utils

import (
	"strings"

	"github.com/gin-gonic/gin"
)

// GetClientIP extracts the real client IP from request headers or direct remote address.
func GetClientIP(c *gin.Context) string {
	// Check X-Forwarded-For first
	xff := c.GetHeader("X-Forwarded-For")
	if xff != "" {
		parts := strings.Split(xff, ",")
		return strings.TrimSpace(parts[0])
	}

	// Check X-Real-IP
	xri := c.GetHeader("X-Real-IP")
	if xri != "" {
		return xri
	}

	// Fall back to direct remote address
	return c.ClientIP()
}
