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
)

// ScanAndCachePermissions scans all registered routes and caches the
// permission tree in Redis for the Permission module UI.
func ScanAndCachePermissions(router *gin.Engine) {
	routes := router.Routes()

	modulePerms := map[string][]string{}

	// Group by module prefix (e.g., "sys:banner" from "sys:banner:page")
	for key, code := range PermissionRouteRegistry {
		parts := strings.Split(code, ":")
		module := ""
		if len(parts) >= 2 {
			module = strings.Join(parts[:len(parts)-1], ":")
		} else {
			module = code
		}
		modulePerms[module] = append(modulePerms[module], key+":"+code)
	}

	// Also scan gin routes that might not be in registry
	for _, r := range routes {
		key := r.Method + ":" + r.Path
		if _, exists := PermissionRouteRegistry[key]; !exists {
			// This route has no permission code — skip
			continue
		}
	}

	ctx := context.Background()
	if db.Redis == nil {
		log.Println("[PermissionScan] Redis not available, skipping cache")
		return
	}

	// Sort for deterministic output
	var modules []string
	for m := range modulePerms {
		modules = append(modules, m)
	}
	sort.Strings(modules)

	tree := map[string]interface{}{}
	for _, m := range modules {
		sort.Strings(modulePerms[m])
		tree[m] = modulePerms[m]
	}

	data, _ := json.Marshal(tree)
	err := db.Redis.Set(ctx, constants.PermissionCacheKey, string(data), 0).Err()
	if err != nil {
		log.Printf("[PermissionScan] cache error: %v", err)
		return
	}
	log.Printf("[PermissionScan] cached %d modules, %d total routes",
		len(modules), len(PermissionRouteRegistry))
}
