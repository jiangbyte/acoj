package utils

import (
	"strings"

	"github.com/gin-gonic/gin"
)

// GetClientIP extracts the real client IP from request headers or direct remote address.
// Order: X-Forwarded-For > X-Real-IP > Proxy-Client-IP > direct remote addr.
func GetClientIP(c *gin.Context) string {
	xff := c.GetHeader("X-Forwarded-For")
	if xff != "" {
		parts := strings.Split(xff, ",")
		return strings.TrimSpace(parts[0])
	}

	xri := c.GetHeader("X-Real-IP")
	if xri != "" {
		return xri
	}

	pip := c.GetHeader("Proxy-Client-IP")
	if pip != "" {
		return pip
	}

	return c.ClientIP()
}
