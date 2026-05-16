package auth

import (
	"context"
	"encoding/json"
	"log"
	"sort"
	"strings"

	"github.com/gin-gonic/gin"

	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/core/enums"
)

// PermissionCacheItem represents a scanned permission entry stored in Redis.
type PermissionCacheItem struct {
	Code     string `json:"code"`
	Module   string `json:"module"`
	Category string `json:"category"`
	Name     string `json:"name"`
}

// ScanAndCachePermissions scans all registered routes and caches the
// permission tree in Redis for the Permission module UI.
func ScanAndCachePermissions(router *gin.Engine) {
	routes := router.Routes()

	// permissionMap: module -> {code -> PermissionCacheItem}
	permissionMap := map[string]map[string]PermissionCacheItem{}

	// Group by module prefix (e.g., "sys:user" from "sys:user:page")
	for key, code := range PermissionRouteRegistry {
		module := moduleFromCode(code)
		category := string(enums.PermissionCategoryBackend)
		if strings.Contains(key, "/api/v1/c/") {
			category = string(enums.PermissionCategoryFrontend)
		}

		if permissionMap[module] == nil {
			permissionMap[module] = map[string]PermissionCacheItem{}
		}
		permissionMap[module][code] = PermissionCacheItem{
			Code:     code,
			Module:   module,
			Category: category,
			Name:     "",
		}
	}

	// Also scan gin routes that might not be in registry
	for _, r := range routes {
		key := r.Method + ":" + r.Path
		if _, exists := PermissionRouteRegistry[key]; !exists {
			continue
		}
	}

	ctx := context.Background()
	if db.Redis == nil {
		log.Println("[PermissionScan] Redis not available, skipping cache")
		return
	}

	// Sort modules for deterministic output
	var modules []string
	for m := range permissionMap {
		modules = append(modules, m)
	}
	sort.Strings(modules)

	tree := map[string]any{}
	for _, m := range modules {
		tree[m] = permissionMap[m]
	}

	data, _ := json.Marshal(tree)
	err := db.Redis.Set(ctx, constants.PermissionCacheKey, string(data), 0).Err()
	if err != nil {
		log.Printf("[PermissionScan] cache error: %v", err)
		return
	}

	totalPerms := 0
	for _, v := range permissionMap {
		totalPerms += len(v)
	}
	log.Printf("[PermissionScan] cached %d modules, %d total permissions",
		len(modules), totalPerms)
}

func moduleFromCode(code string) string {
	parts := strings.Split(code, ":")
	if len(parts) >= 2 {
		return strings.Join(parts[:len(parts)-1], ":")
	}
	return code
}
