package auth

import "strings"

type PermissionMatcher struct{}

var Matcher = &PermissionMatcher{}

// Match checks if a permission matches a pattern (supports * and ** wildcards)
func (m *PermissionMatcher) Match(pattern, permission string) bool {
	if pattern == "" || permission == "" {
		return false
	}
	if pattern == permission || pattern == "*" || pattern == "**" {
		return true
	}

	sep := m.detectSeparator(pattern)

	if strings.Contains(pattern, "**") {
		return m.matchDoubleWildcard(pattern, permission, sep)
	} else if strings.Contains(pattern, "*") {
		return m.matchSingleWildcard(pattern, permission, sep)
	}
	return pattern == permission
}

// HasPermission checks if any permission in the list matches the required code
func (m *PermissionMatcher) HasPermission(required string, permissions []string) bool {
	for _, p := range permissions {
		if m.Match(p, required) {
			return true
		}
	}
	return false
}

// HasPermissionAnd checks if all required permissions are present
func (m *PermissionMatcher) HasPermissionAnd(required []string, permissions []string) bool {
	for _, r := range required {
		if !m.HasPermission(r, permissions) {
			return false
		}
	}
	return true
}

// HasPermissionOr checks if any required permission is present
func (m *PermissionMatcher) HasPermissionOr(required []string, permissions []string) bool {
	for _, r := range required {
		if m.HasPermission(r, permissions) {
			return true
		}
	}
	return false
}

func (m *PermissionMatcher) detectSeparator(pattern string) string {
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

func (m *PermissionMatcher) matchSingleWildcard(pattern, permission, sep string) bool {
	patternParts := strings.Split(pattern, sep)
	permParts := strings.Split(permission, sep)

	if len(patternParts) != len(permParts) {
		return false
	}
	for i, pp := range patternParts {
		if pp == "*" {
			continue
		}
		if pp != permParts[i] {
			return false
		}
	}
	return true
}

func (m *PermissionMatcher) matchDoubleWildcard(pattern, permission, sep string) bool {
	patternParts := strings.Split(pattern, sep)
	permParts := strings.Split(permission, sep)
	return m.matchPartsWithDoubleWildcard(patternParts, permParts)
}

func (m *PermissionMatcher) matchPartsWithDoubleWildcard(pattern, permission []string) bool {
	if len(pattern) == 0 {
		return len(permission) == 0
	}
	if len(permission) == 0 {
		for _, p := range pattern {
			if p != "**" {
				return false
			}
		}
		return true
	}
	if pattern[0] == "**" {
		if len(pattern) == 1 {
			return true
		}
		for i := 0; i <= len(permission); i++ {
			if m.matchPartsWithDoubleWildcard(pattern[1:], permission[i:]) {
				return true
			}
		}
		return false
	}
	if pattern[0] == "*" {
		return m.matchPartsWithDoubleWildcard(pattern[1:], permission[1:])
	}
	if pattern[0] == permission[0] {
		return m.matchPartsWithDoubleWildcard(pattern[1:], permission[1:])
	}
	return false
}
