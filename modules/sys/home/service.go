package home

import (
	"context"
	"time"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysnotice"
	"hei-gin/ent/gen/sysquickaction"
	"hei-gin/ent/gen/sysresource"
)

// HomeGet returns the home page aggregated data.
func HomeGet(c *gin.Context) *HomeVO {
	userID := auth.GetLoginIDDefaultNull(c)

	result := &HomeVO{
		QuickActions:       make([]QuickActionVO, 0),
		AvailableResources: make([]QuickActionVO, 0),
		Notices:            make([]HomeNotice, 0),
		Stats:              HomeStats{},
	}

	if userID != "" {
		result.QuickActions = findQuickActionsByUserID(userID)
		result.AvailableResources = getAvailableResources(userID)
	}

	result.Notices = getNotices()
	result.Stats.TotalUsers = getUserCount()

	return result
}

// HomeAddQuickAction adds a quick action for the current user.
func HomeAddQuickAction(c *gin.Context, param *AddQuickActionParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return
	}

	ctx := context.Background()

	// Check if already exists
	exists, err := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserID(userID)).
		Where(sysquickaction.ResourceID(param.ResourceID)).
		Exist(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询快捷方式失败: "+err.Error(), 500))
	}
	if exists {
		return
	}

	// Count existing quick actions for user
	count, err := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserID(userID)).
		Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询快捷方式数量失败: "+err.Error(), 500))
	}

	now := time.Now()
	_, err = db.Client.SysQuickAction.Create().
		SetID(utils.GenerateID()).
		SetUserID(userID).
		SetResourceID(param.ResourceID).
		SetSortCode((count + 1) * 10).
		SetCreatedAt(now).
		SetUpdatedAt(now).
		SetCreatedBy(userID).
		SetUpdatedBy(userID).
		Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加快捷方式失败: "+err.Error(), 500))
	}
}

// HomeRemoveQuickAction removes a quick action by ID.
func HomeRemoveQuickAction(c *gin.Context, param *RemoveQuickActionParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return
	}

	ctx := context.Background()
	err := db.Client.SysQuickAction.DeleteOneID(param.ID).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除快捷方式失败: "+err.Error(), 500))
	}
}

// HomeSortQuickActions updates the sort order of quick actions.
func HomeSortQuickActions(c *gin.Context, param *SortQuickActionParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return
	}

	ctx := context.Background()
	for idx, id := range param.IDs {
		err := db.Client.SysQuickAction.UpdateOneID(id).
			SetSortCode((idx + 1) * 10).
			Exec(ctx)
		if err != nil {
			panic(exception.NewBusinessError("排序快捷方式失败: "+err.Error(), 500))
		}
	}
}

// findQuickActionsByUserID retrieves the user's quick actions with resource details.
func findQuickActionsByUserID(userID string) []QuickActionVO {
	ctx := context.Background()

	actions, err := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserID(userID)).
		Order(sysquickaction.BySortCode(sql.OrderAsc())).
		Order(sysquickaction.ByCreatedAt(sql.OrderAsc())).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询快捷方式失败: "+err.Error(), 500))
	}

	if len(actions) == 0 {
		return make([]QuickActionVO, 0)
	}

	// Collect resource IDs
	resourceIDs := make([]string, 0, len(actions))
	for _, a := range actions {
		resourceIDs = append(resourceIDs, a.ResourceID)
	}

	// Query resources
	resources, err := db.Client.SysResource.Query().
		Where(sysresource.IDIn(resourceIDs...)).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询资源失败: "+err.Error(), 500))
	}

	// Build resource map
	resourceMap := make(map[string]*ent.SysResource, len(resources))
	for _, r := range resources {
		resourceMap[r.ID] = r
	}

	// Convert to VOs
	vos := make([]QuickActionVO, 0, len(actions))
	for _, a := range actions {
		vo := QuickActionVO{
			ID:         a.ID,
			ResourceID: a.ResourceID,
			SortCode:   a.SortCode,
		}

		if r, ok := resourceMap[a.ResourceID]; ok {
			vo.Name = r.Name
			vo.Type = r.Type
			if r.Icon != nil {
				vo.Icon = *r.Icon
			}
			if r.RoutePath != nil {
				vo.RoutePath = *r.RoutePath
			}
			if r.ParentID != nil {
				vo.ParentID = *r.ParentID
			}
		}

		vos = append(vos, vo)
	}

	return vos
}

// getAvailableResources retrieves resources that the user has NOT yet added as quick actions.
func getAvailableResources(userID string) []QuickActionVO {
	ctx := context.Background()

	// Get existing quick action resource IDs for this user
	existingActions, err := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserID(userID)).
		Select(sysquickaction.FieldResourceID).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询快捷方式失败: "+err.Error(), 500))
	}

	excludedIDs := make([]string, 0, len(existingActions))
	for _, a := range existingActions {
		excludedIDs = append(excludedIDs, a.ResourceID)
	}

	// Build query
	query := db.Client.SysResource.Query().
		Where(sysresource.StatusEQ("ENABLED")).
		Where(sysresource.TypeIn("MENU", "DIRECTORY")).
		Order(sysresource.BySortCode(sql.OrderAsc())).
		Limit(50)

	if len(excludedIDs) > 0 {
		query = query.Where(sysresource.IDNotIn(excludedIDs...))
	}

	resources, err := query.All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询资源失败: "+err.Error(), 500))
	}

	vos := make([]QuickActionVO, 0, len(resources))
	for _, r := range resources {
		vo := QuickActionVO{
			ResourceID: r.ID,
			Name:       r.Name,
			Type:       r.Type,
			SortCode:   r.SortCode,
		}
		if r.Icon != nil {
			vo.Icon = *r.Icon
		}
		if r.RoutePath != nil {
			vo.RoutePath = *r.RoutePath
		}
		if r.ParentID != nil {
			vo.ParentID = *r.ParentID
		}

		vos = append(vos, vo)
	}

	return vos
}

// getNotices retrieves the top 5 enabled notices.
func getNotices() []HomeNotice {
	ctx := context.Background()

	notices, err := db.Client.SysNotice.Query().
		Where(sysnotice.StatusEQ("ENABLED")).
		Order(sysnotice.ByIsTop(sql.OrderDesc())).
		Order(sysnotice.ByCreatedAt(sql.OrderDesc())).
		Limit(5).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询通知失败: "+err.Error(), 500))
	}

	vos := make([]HomeNotice, 0, len(notices))
	for _, n := range notices {
		vo := HomeNotice{
			ID:    n.ID,
			Title: n.Title,
			Level: n.Level,
		}
		if n.CreatedAt != nil {
			vo.CreatedAt = n.CreatedAt.Format("2006-01-02 15:04:05")
		}
		vos = append(vos, vo)
	}

	return vos
}

// getUserCount returns the total number of users.
func getUserCount() int {
	ctx := context.Background()

	count, err := db.Client.SysUser.Query().Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户数量失败: "+err.Error(), 500))
	}

	return count
}
