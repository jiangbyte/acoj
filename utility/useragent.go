package utility

import "strings"

// GetBrowser extracts the browser name from a User-Agent string.
// Returns "-" if unknown or the UA string is empty.
func GetBrowser(ua string) string {
	if ua == "" {
		return "-"
	}
	uaLower := strings.ToLower(ua)

	switch {
	case strings.Contains(uaLower, "edg"):
		return "Edge"
	case strings.Contains(uaLower, "chrome"):
		return "Chrome"
	case strings.Contains(uaLower, "safari"):
		return "Safari"
	case strings.Contains(uaLower, "firefox"):
		return "Firefox"
	case strings.Contains(uaLower, "opera") || strings.Contains(uaLower, "opr"):
		return "Opera"
	default:
		return "-"
	}
}

// GetOS extracts the operating system name from a User-Agent string.
// Returns "-" if unknown or the UA string is empty.
func GetOS(ua string) string {
	if ua == "" {
		return "-"
	}
	uaLower := strings.ToLower(ua)

	switch {
	case strings.Contains(uaLower, "windows"):
		return "Windows"
	case strings.Contains(uaLower, "mac os") || strings.Contains(uaLower, "macintosh"):
		return "macOS"
	case strings.Contains(uaLower, "linux") && !strings.Contains(uaLower, "android"):
		return "Linux"
	case strings.Contains(uaLower, "android"):
		return "Android"
	case strings.Contains(uaLower, "iphone") || strings.Contains(uaLower, "ipad") || strings.Contains(uaLower, "ipod"):
		return "iOS"
	default:
		return "-"
	}
}
