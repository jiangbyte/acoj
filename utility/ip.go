package utility

import (
	"strings"

	"github.com/gogf/gf/v2/net/ghttp"
)

const (
	// Unknown represents an unknown IP or location.
	Unknown = "unknown"
)

// GetClientIP extracts the real client IP from the request, checking
// common proxy headers first, then falling back to the direct connection IP.
func GetClientIP(r *ghttp.Request) string {
	// Check X-Forwarded-For header
	ip := r.GetHeader("X-Forwarded-For")
	if ip != "" && !strings.EqualFold(ip, Unknown) {
		parts := strings.Split(ip, ",")
		if len(parts) > 0 {
			return strings.TrimSpace(parts[0])
		}
	}

	// Check X-Real-IP header
	ip = r.GetHeader("X-Real-IP")
	if ip != "" && !strings.EqualFold(ip, Unknown) {
		return ip
	}

	// Check Proxy-Client-IP header
	ip = r.GetHeader("Proxy-Client-IP")
	if ip != "" && !strings.EqualFold(ip, Unknown) {
		return ip
	}

	// Fallback to GoFrame's built-in client IP detection
	return r.GetClientIp()
}

// GetCityInfo returns the city/location info for a given IP address.
// This is a placeholder; integrate with an IP database (e.g. ip2region) when needed.
func GetCityInfo(ip string) string {
	if ip == "" || ip == Unknown || ip == "127.0.0.1" || ip == "::1" {
		return ""
	}
	return ""
}
