package auth

import "strings"

// PermissionMatcher 权限匹配工具，支持冒号/点/斜杠分隔符，*单层通配符，**多层通配符
type PermissionMatcher struct{}

func (m *PermissionMatcher) Match(pattern, permission string) bool {
	if pattern == "" || permission == "" {
		return false
	}
	if pattern == permission || pattern == "*" || pattern == "**" {
		return true
	}

	sep := detectSeparator(pattern)

	if strings.Contains(pattern, "**") {
		return matchDoubleWildcard(pattern, permission, sep)
	}
	if strings.Contains(pattern, "*") {
		return matchSingleWildcard(pattern, permission, sep)
	}
	return pattern == permission
}

func (m *PermissionMatcher) MatchAny(patterns []string, permission string) bool {
	for _, p := range patterns {
		if m.Match(p, permission) {
			return true
		}
	}
	return false
}

func (m *PermissionMatcher) MatchAll(patterns []string, permission string) bool {
	for _, p := range patterns {
		if !m.Match(p, permission) {
			return false
		}
	}
	return true
}

func (m *PermissionMatcher) HasPermission(required string, permissions []string) bool {
	for _, p := range permissions {
		if m.Match(p, required) {
			return true
		}
	}
	return false
}

func (m *PermissionMatcher) HasPermissionAnd(requiredList, permissions []string) bool {
	for _, r := range requiredList {
		if !m.HasPermission(r, permissions) {
			return false
		}
	}
	return true
}

func (m *PermissionMatcher) HasPermissionOr(requiredList, permissions []string) bool {
	for _, r := range requiredList {
		if m.HasPermission(r, permissions) {
			return true
		}
	}
	return false
}

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

func matchSingleWildcard(pattern, permission, sep string) bool {
	pParts := strings.Split(pattern, sep)
	permParts := strings.Split(permission, sep)
	if len(pParts) != len(permParts) {
		return false
	}
	for i, pp := range pParts {
		if pp == "*" {
			continue
		}
		if pp != permParts[i] {
			return false
		}
	}
	return true
}

func matchDoubleWildcard(pattern, permission, sep string) bool {
	pParts := strings.Split(pattern, sep)
	permParts := strings.Split(permission, sep)
	return matchPartsWithDoubleWildcard(pParts, permParts)
}

func matchPartsWithDoubleWildcard(patternParts, permissionParts []string) bool {
	if len(patternParts) == 0 {
		return len(permissionParts) == 0
	}
	if len(permissionParts) == 0 {
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
