package home

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func GetHome(ctx context.Context) (g.Map, error) {
	loginId := getLoginId(ctx)

	// Get quick actions for current user, joined with sys_resource
	quickActions, err := dao.SysQuickAction.Ctx().Ctx(ctx).
		Where("created_by", loginId).
		OrderAsc("sort_code").
		All()
	if err != nil {
		return nil, err
	}

	quickActionList := make([]g.Map, 0)
	resourceIds := make([]string, 0)
	for _, qa := range quickActions {
		resourceIds = append(resourceIds, qa["resource_id"].String())
		quickActionList = append(quickActionList, g.Map{
			"id":        qa["id"].String(),
			"sort_code": qa["sort_code"].Int(),
		})
	}

	// Enrich quick actions with resource info
	if len(resourceIds) > 0 {
		resources, err := dao.SysResource.Ctx().Ctx(ctx).
			WherePri(resourceIds).
			Fields("id", "name", "icon", "route_path").
			All()
		if err == nil {
			resourceMap := make(map[string]g.Map)
			for _, r := range resources {
				resourceMap[r["id"].String()] = g.Map{
					"name":       r["name"].String(),
					"icon":       r["icon"].String(),
					"route_path": r["route_path"].String(),
				}
			}
			for _, qa := range quickActionList {
				rid := ""
				for _, raw := range quickActions {
					if raw["id"].String() == qa["id"].(string) {
						rid = raw["resource_id"].String()
						break
					}
				}
				if res, ok := resourceMap[rid]; ok {
					qa["name"] = res["name"]
					qa["icon"] = res["icon"]
					qa["route_path"] = res["route_path"]
				}
			}
		}
	}

	// Get all resources
	allResources, err := dao.SysResource.Ctx().Ctx(ctx).
		Fields("id", "code", "name", "icon", "route_path", "category", "type").
		OrderAsc("sort_code").
		All()
	if err != nil {
		return nil, err
	}

	availableResources := make([]g.Map, 0)
	usedIds := make(map[string]bool)
	for _, rid := range resourceIds {
		usedIds[rid] = true
	}
	for _, r := range allResources {
		if !usedIds[r["id"].String()] {
			availableResources = append(availableResources, g.Map{
				"id":         r["id"].String(),
				"code":       r["code"].String(),
				"name":       r["name"].String(),
				"icon":       r["icon"].String(),
				"route_path": r["route_path"].String(),
				"category":   r["category"].String(),
				"type":       r["type"].String(),
			})
		}
	}

	// Get notice count
	noticeCount, _ := dao.SysNotice.Ctx().Ctx(ctx).Count()

	// Get total users
	totalUsers, _ := dao.SysUser.Ctx().Ctx(ctx).Count()

	return g.Map{
		"quick_actions":       quickActionList,
		"available_resources": availableResources,
		"notice_count":        noticeCount,
		"stats": g.Map{
			"total_users": totalUsers,
		},
	}, nil
}

func AddQuickAction(ctx context.Context, resourceId string) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysQuickAction.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"resource_id": resourceId,
		"created_by":  loginId,
	})
	return err
}

func RemoveQuickAction(ctx context.Context, id string) error {
	_, err := dao.SysQuickAction.Ctx().Ctx(ctx).WherePri(id).Delete()
	return err
}

func SortQuickAction(ctx context.Context, ids []string) error {
	for i, id := range ids {
		_, err := dao.SysQuickAction.Ctx().Ctx(ctx).WherePri(id).Update(g.Map{
			"sort_code": i + 1,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}
