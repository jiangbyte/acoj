package utils

import "strings"

// GetBrowser extracts the browser family from a User-Agent string.
func GetBrowser(ua string) string {
	if ua == "" {
		return "-"
	}

	lower := strings.ToLower(ua)

	switch {
	case strings.Contains(lower, "edge"):
		return "Edge"
	case strings.Contains(lower, "opr") || strings.Contains(lower, "opera"):
		return "Opera"
	case strings.Contains(lower, "chrome"):
		return "Chrome"
	case strings.Contains(lower, "safari"):
		return "Safari"
	case strings.Contains(lower, "firefox"):
		return "Firefox"
	case strings.Contains(lower, "msie") || strings.Contains(lower, "trident"):
		return "Internet Explorer"
	default:
		return "-"
	}
}

// GetOS extracts the OS family from a User-Agent string.
func GetOS(ua string) string {
	if ua == "" {
		return "-"
	}

	lower := strings.ToLower(ua)

	switch {
	case strings.Contains(lower, "windows"):
		return "Windows"
	case strings.Contains(lower, "mac os") || strings.Contains(lower, "macintosh"):
		return "macOS"
	case strings.Contains(lower, "android"):
		return "Android"
	case strings.Contains(lower, "ios") || strings.Contains(lower, "iphone") || strings.Contains(lower, "ipad"):
		return "iOS"
	case strings.Contains(lower, "linux"):
		return "Linux"
	default:
		return "-"
	}
}
