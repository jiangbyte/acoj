package auth

import (
	"strings"
)

// Match checks if a permission string matches a pattern.
// Supports colon (:), dot (.), and slash (/) as separators.
// Wildcards: * matches a single level, ** matches multiple levels.
func Match(pattern, permission string) bool {
	if pattern == "" || permission == "" {
		return false
	}

	if pattern == permission {
		return true
	}

	if pattern == "*" || pattern == "**" {
		return true
	}

	separator := detectSeparator(pattern)

	if strings.Contains(pattern, "**") {
		return matchDoubleWildcard(pattern, permission, separator)
	} else if strings.Contains(pattern, "*") {
		return matchSingleWildcard(pattern, permission, separator)
	}

	return pattern == permission
}

// MatchAny returns true if permission matches any of the patterns.
func MatchAny(patterns []string, permission string) bool {
	for _, p := range patterns {
		if Match(p, permission) {
			return true
		}
	}
	return false
}

// MatchAll returns true if permission matches all of the patterns.
func MatchAll(patterns []string, permission string) bool {
	for _, p := range patterns {
		if !Match(p, permission) {
			return false
		}
	}
	return true
}

// MatchPermission checks if a required pattern matches any of the given permissions.
func MatchPermission(required string, permissions []string) bool {
	for _, p := range permissions {
		if Match(p, required) {
			return true
		}
	}
	return false
}

// MatchPermissionsAnd checks if all required patterns match in the given permissions.
func MatchPermissionsAnd(required []string, permissions []string) bool {
	for _, r := range required {
		if !MatchPermission(r, permissions) {
			return false
		}
	}
	return true
}

// MatchPermissionsOr checks if any of the required patterns matches in the given permissions.
func MatchPermissionsOr(required []string, permissions []string) bool {
	for _, r := range required {
		if MatchPermission(r, permissions) {
			return true
		}
	}
	return false
}

// detectSeparator detects the separator character used in the pattern.
// Priority: "/" > ":" > "." > default ":"
func detectSeparator(pattern string) string {
	if strings.Contains(pattern, "/") {
		return "/"
	}
	if strings.Contains(pattern, ":") {
		return ":"
	}
	if strings.Contains(pattern, ".") {
		return "."
	}
	return ":"
}

// matchSingleWildcard matches a pattern containing single-level wildcard (*).
// Both pattern and permission must have the same number of parts.
func matchSingleWildcard(pattern, permission, separator string) bool {
	patternParts := strings.Split(pattern, separator)
	permissionParts := strings.Split(permission, separator)

	if len(patternParts) != len(permissionParts) {
		return false
	}

	for i, pPart := range patternParts {
		permPart := permissionParts[i]
		if pPart == "*" {
			continue
		}
		if pPart != permPart {
			return false
		}
	}

	return true
}

// matchDoubleWildcard delegates to matchPartsWithDoubleWildcard after splitting.
func matchDoubleWildcard(pattern, permission, separator string) bool {
	patternParts := strings.Split(pattern, separator)
	permissionParts := strings.Split(permission, separator)
	return matchPartsWithDoubleWildcard(patternParts, permissionParts)
}

// matchPartsWithDoubleWildcard recursively matches patterns with ** wildcard.
func matchPartsWithDoubleWildcard(patternParts, permissionParts []string) bool {
	if len(patternParts) == 0 {
		return len(permissionParts) == 0
	}

	if len(permissionParts) == 0 {
		// All remaining pattern parts must be "**"
		for _, p := range patternParts {
			if p != "**" {
				return false
			}
		}
		return true
	}

	if patternParts[0] == "**" {
		if len(patternParts) == 1 {
			return true
		}
		for i := 0; i <= len(permissionParts); i++ {
			if matchPartsWithDoubleWildcard(patternParts[1:], permissionParts[i:]) {
				return true
			}
		}
		return false
	}

	if patternParts[0] == "*" {
		return matchPartsWithDoubleWildcard(patternParts[1:], permissionParts[1:])
	}

	if patternParts[0] == permissionParts[0] {
		return matchPartsWithDoubleWildcard(patternParts[1:], permissionParts[1:])
	}

	return false
}
