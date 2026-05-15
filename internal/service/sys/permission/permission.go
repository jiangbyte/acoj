package permission

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:permission:page", "sys/permission", "BACKEND", "权限查询")
	auth.RegisterPermission("sys:permission:scan", "sys/permission", "BACKEND", "权限扫描")
	auth.RegisterPermission("sys:permission:modules", "sys/permission", "BACKEND", "权限模块列表")
	auth.RegisterPermission("sys:permission:by-module", "sys/permission", "BACKEND", "按模块获取权限")
}

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	modules := auth.GetModulesFromRedis(ctx)
	if modules == nil {
		return utility.NewPageRes([]g.Map{}, 0, current, size), nil
	}

	var allPermissions []g.Map
	for _, module := range modules {
		perms := auth.GetPermissionsByModuleFromRedis(ctx, module)
		for _, p := range perms {
			allPermissions = append(allPermissions, g.Map{
				"code":     p["code"],
				"module":   p["module"],
				"category": p["category"],
				"name":     p["name"],
			})
		}
	}

	total := len(allPermissions)
	start := (current - 1) * size
	if start >= total {
		return utility.NewPageRes([]g.Map{}, total, current, size), nil
	}
	end := start + size
	if end > total {
		end = total
	}
	pageList := allPermissions[start:end]
	if pageList == nil {
		pageList = []g.Map{}
	}
	return utility.NewPageRes(pageList, total, current, size), nil
}

func Scan(ctx context.Context) error {
	auth.RunPermissionScan(ctx)
	return nil
}

func GetModules(ctx context.Context) ([]string, error) {
	modules := auth.GetModulesFromRedis(ctx)
	if modules == nil {
		return []string{}, nil
	}
	return modules, nil
}

func GetListByModule(ctx context.Context, module string) ([]g.Map, error) {
	perms := auth.GetPermissionsByModuleFromRedis(ctx, module)
	var result []g.Map
	for _, p := range perms {
		result = append(result, g.Map{
			"code":     p["code"],
			"module":   p["module"],
			"category": p["category"],
			"name":     p["name"],
		})
	}
	if result == nil {
		result = []g.Map{}
	}
	return result, nil
}
