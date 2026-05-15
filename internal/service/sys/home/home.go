package home

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	api "hei-goframe/api/sys/home/v1"
	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func GetHome(ctx context.Context) (*api.GetHomeRes, error) {
	loginId := getLoginId(ctx)

	// Initialize empty slices (not nil) so they serialize as [] in JSON
	quickActions := make([]*api.QuickActionItem, 0)
	availableResources := make([]*api.ResourceItem, 0)

	if loginId != "" {
		// 1. Get quick actions joined with resources
		qaRecords, err := dao.SysQuickAction.Ctx().Ctx(ctx).
			LeftJoin("sys_resource", "sys_quick_action.resource_id = sys_resource.id").
			Fields(
				"sys_quick_action.id",
				"sys_quick_action.resource_id",
				"sys_quick_action.sort_code",
				"sys_resource.name",
				"sys_resource.icon",
				"sys_resource.route_path",
				"sys_resource.parent_id",
				"sys_resource.type",
			).
			Where("sys_quick_action.user_id", loginId).
			OrderAsc("sys_quick_action.sort_code").
			All()
		if err != nil {
			return nil, err
		}

		for _, r := range qaRecords {
			quickActions = append(quickActions, &api.QuickActionItem{
				Id:         r["id"].String(),
				ResourceId: r["resource_id"].String(),
				SortCode:   r["sort_code"].Int(),
				Name:       r["name"].String(),
				Icon:       r["icon"].String(),
				RoutePath:  r["route_path"].String(),
				ParentId:   r["parent_id"].String(),
				Type:       r["type"].String(),
			})
		}

		// 2. Get available resources (ENABLED + MENU/DIRECTORY, exclude already added, limit 50)
		usedRecords, _ := dao.SysQuickAction.Ctx().Ctx(ctx).
			Where("user_id", loginId).
			Fields("resource_id").
			All()

		usedIds := make([]string, 0)
		for _, r := range usedRecords {
			usedIds = append(usedIds, r["resource_id"].String())
		}

		resModel := dao.SysResource.Ctx().Ctx(ctx).
			Where(dao.SysResource.Columns.Status, consts.StatusEnabled).
			WhereIn(dao.SysResource.Columns.Type, g.Slice{"MENU", "DIRECTORY"}).
			OrderAsc(dao.SysResource.Columns.SortCode).
			Limit(50)

		if len(usedIds) > 0 {
			resModel = resModel.WhereNotIn(dao.SysResource.Columns.Id, usedIds)
		}

		resRecords, err := resModel.All()
		if err != nil {
			return nil, err
		}

		for _, r := range resRecords {
			availableResources = append(availableResources, &api.ResourceItem{
				Id:         r[dao.SysResource.Columns.Id].String(),
				ResourceId: r[dao.SysResource.Columns.Id].String(),
				ParentId:   r[dao.SysResource.Columns.ParentId].String(),
				Type:       r[dao.SysResource.Columns.Type].String(),
				Name:       r[dao.SysResource.Columns.Name].String(),
				Icon:       r[dao.SysResource.Columns.Icon].String(),
				RoutePath:  r[dao.SysResource.Columns.RoutePath].String(),
				SortCode:   r[dao.SysResource.Columns.SortCode].Int(),
			})
		}
	}

	// 3. Get notices (top 5 enabled, ordered by is_top DESC, created_at DESC)
	notices := make([]*api.HomeNoticeItem, 0)
	noticeRecords, err := dao.SysNotice.Ctx().Ctx(ctx).
		Where(dao.SysNotice.Columns.Status, consts.StatusEnabled).
		OrderDesc(dao.SysNotice.Columns.IsTop).
		OrderDesc(dao.SysNotice.Columns.CreatedAt).
		Limit(5).
		All()
	if err != nil {
		return nil, err
	}

	for _, r := range noticeRecords {
		notices = append(notices, &api.HomeNoticeItem{
			Id:        r[dao.SysNotice.Columns.Id].String(),
			Title:     r[dao.SysNotice.Columns.Title].String(),
			Level:     r[dao.SysNotice.Columns.Level].String(),
			CreatedAt: r[dao.SysNotice.Columns.CreatedAt].GTime(),
		})
	}

	// 4. Get stats
	totalUsers, _ := dao.SysUser.Ctx().Ctx(ctx).Count()
	stats := &api.HomeStats{
		TotalUsers: int64(totalUsers),
	}

	return &api.GetHomeRes{
		QuickActions:       quickActions,
		AvailableResources: availableResources,
		Notices:            notices,
		Stats:              stats,
	}, nil
}

func AddQuickAction(ctx context.Context, resourceId string) error {
	loginId := getLoginId(ctx)
	if loginId == "" {
		return nil
	}

	// Check for existing duplicate by user_id + resource_id
	count, err := dao.SysQuickAction.Ctx().Ctx(ctx).
		Where(dao.SysQuickAction.Columns.UserId, loginId).
		Where(dao.SysQuickAction.Columns.ResourceId, resourceId).
		Count()
	if err != nil {
		return err
	}
	if count > 0 {
		return nil
	}

	// Get count for sort_code = (count + 1) * 10
	totalCount, err := dao.SysQuickAction.Ctx().Ctx(ctx).
		Where(dao.SysQuickAction.Columns.UserId, loginId).
		Count()
	if err != nil {
		return err
	}

	_, err = dao.SysQuickAction.Ctx().Ctx(ctx).Insert(g.Map{
		dao.SysQuickAction.Columns.Id:         utility.GenerateID(),
		dao.SysQuickAction.Columns.UserId:     loginId,
		dao.SysQuickAction.Columns.ResourceId: resourceId,
		dao.SysQuickAction.Columns.SortCode:   (totalCount + 1) * 10,
		dao.SysQuickAction.Columns.CreatedBy:  loginId,
	})
	return err
}

func RemoveQuickAction(ctx context.Context, id string) error {
	_, err := dao.SysQuickAction.Ctx().Ctx(ctx).WherePri(id).Delete()
	return err
}

func SortQuickAction(ctx context.Context, ids []string) error {
	loginId := getLoginId(ctx)
	if loginId == "" {
		return nil
	}

	// Fetch only entities owned by the current user
	entities, err := dao.SysQuickAction.Ctx().Ctx(ctx).
		WherePri(ids).
		Where(dao.SysQuickAction.Columns.UserId, loginId).
		All()
	if err != nil {
		return err
	}

	// Build entity ID map for ownership check
	entityMap := make(map[string]bool)
	for _, e := range entities {
		entityMap[e[dao.SysQuickAction.Columns.Id].String()] = true
	}

	// Update sort codes, only for entities owned by the current user
	for i, qaId := range ids {
		if entityMap[qaId] {
			_, err := dao.SysQuickAction.Ctx().Ctx(ctx).
				WherePri(qaId).
				Update(g.Map{
					dao.SysQuickAction.Columns.SortCode: (i + 1) * 10,
				})
			if err != nil {
				return err
			}
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
