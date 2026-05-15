package auth

import (
	"context"
	"encoding/json"
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/internal/consts"
)

// PermissionRegistry 注册权限码与其所属模块的映射
type PermissionRegistry struct {
	Code     string
	Module   string
	Category string
	Name     string
}

var registeredPermissions []PermissionRegistry

// RegisterPermission 注册单个权限码
func RegisterPermission(code, module, category, name string) {
	registeredPermissions = append(registeredPermissions, PermissionRegistry{
		Code:     code,
		Module:   module,
		Category: category,
		Name:     name,
	})
}

// RegisterPermissions 批量注册权限码
func RegisterPermissions(perms []PermissionRegistry) {
	registeredPermissions = append(registeredPermissions, perms...)
}

// RunPermissionScan 同步所有已注册权限到 Redis 缓存
func RunPermissionScan(ctx context.Context) {
	tree := buildPermissionTree(registeredPermissions)
	if len(tree) > 0 {
		data, _ := json.Marshal(tree)
		g.Redis().Set(ctx, consts.PermissionCacheKey, data)
	}
}

func buildPermissionTree(permissions []PermissionRegistry) map[string]map[string]interface{} {
	tree := make(map[string]map[string]interface{})
	for _, p := range permissions {
		if p.Module == "" {
			p.Module = getModuleFromCode(p.Code)
		}
		if p.Category == "" {
			p.Category = consts.PermissionCategoryBackend
		}
		if _, ok := tree[p.Module]; !ok {
			tree[p.Module] = make(map[string]interface{})
		}
		tree[p.Module][p.Code] = map[string]interface{}{
			"code":     p.Code,
			"module":   p.Module,
			"category": p.Category,
			"name":     p.Name,
		}
	}
	return tree
}

func getModuleFromCode(code string) string {
	for i := len(code) - 1; i >= 0; i-- {
		if code[i] == ':' {
			return code[:i]
		}
	}
	return code
}

// GetModulesFromRedis 从 Redis 获取所有模块列表
func GetModulesFromRedis(ctx context.Context) []string {
	data, err := g.Redis().Get(ctx, consts.PermissionCacheKey)
	if err != nil || data.IsNil() {
		return nil
	}
	var tree map[string]interface{}
	if json.Unmarshal(data.Bytes(), &tree) != nil {
		return nil
	}
	modules := make([]string, 0, len(tree))
	for m := range tree {
		modules = append(modules, m)
	}
	return modules
}

// GetPermissionsByModuleFromRedis 从 Redis 获取指定模块的权限列表
func GetPermissionsByModuleFromRedis(ctx context.Context, module string) []map[string]interface{} {
	data, err := g.Redis().Get(ctx, consts.PermissionCacheKey)
	if err != nil || data.IsNil() {
		return nil
	}
	var tree map[string]map[string]interface{}
	if json.Unmarshal(data.Bytes(), &tree) != nil {
		return nil
	}
	perms, ok := tree[module]
	if !ok {
		return nil
	}
	result := make([]map[string]interface{}, 0, len(perms))
	for _, v := range perms {
		if m, ok := v.(map[string]interface{}); ok {
			result = append(result, m)
		}
	}
	return result
}
