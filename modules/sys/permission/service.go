package permission

import (
	"context"
	"encoding/json"
	"sort"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/constants"
	"hei-gin/core/db"
	"hei-gin/ent"
	"hei-gin/ent/syspermission"
)

// GinEngine holds a reference to the gin engine for permission cache refresh.
// Set this during application startup (e.g., from core/app.go or main.go).
var GinEngine *gin.Engine

type PermissionVO struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Code        string `json:"code"`
	Category    string `json:"category"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type PermissionByModuleReq struct {
	Module string `form:"module" json:"module" binding:"required"`
}

func toVO(p *ent.SysPermission) PermissionVO {
	vo := PermissionVO{
		ID:          p.ID,
		Name:        p.Name,
		Code:        p.Code,
		Category:    p.Category,
		Description: p.Description,
		SortCode:    p.SortCode,
		Status:      p.Status,
		CreatedAt:   p.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   p.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   p.CreatedBy,
		UpdatedBy:   p.UpdatedBy,
	}
	return vo
}

// GetModules reads the permission cache key from Redis, parses the JSON,
// and returns the module names (sorted keys) as a string array.
// If not in Redis, triggers ScanAndCachePermissions.
func GetModules() ([]string, error) {
	ctx := context.Background()

	data, err := db.Redis.Get(ctx, constants.PermissionCacheKey).Result()
	if err != nil {
		// Not in cache — trigger rescan if we have the engine
		if GinEngine != nil {
			auth.ScanAndCachePermissions(GinEngine)
		}
		return []string{}, nil
	}

	var tree map[string]interface{}
	if err := json.Unmarshal([]byte(data), &tree); err != nil {
		return []string{}, nil
	}

	var modules []string
	for m := range tree {
		modules = append(modules, m)
	}
	sort.Strings(modules)
	return modules, nil
}

// GetPermissionsByModule queries sys_permission by code prefix matching the module.
func GetPermissionsByModule(module string) ([]*ent.SysPermission, error) {
	ctx := context.Background()
	items, err := db.Client.SysPermission.Query().
		Where(syspermission.CodeHasPrefix(module + ":")).
		Order(ent.Asc(syspermission.FieldSortCode)).
		All(ctx)
	if err != nil {
		return nil, err
	}
	return items, nil
}
