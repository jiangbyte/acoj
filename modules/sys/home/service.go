package home

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/sysnotice"
	"hei-gin/ent/gen/sysquickaction"
	"hei-gin/ent/gen/sysresource"
)

type QuickActionVO struct {
	ID         string `json:"id"`
	ResourceID string `json:"resource_id"`
	Name       string `json:"name"`
	Icon       string `json:"icon"`
	RoutePath  string `json:"route_path"`
	SortCode   int    `json:"sort_code"`
	ParentID   string `json:"parent_id,omitempty"`
	Type       string `json:"type,omitempty"`
}

type HomeNotice struct {
	ID        string `json:"id"`
	Title     string `json:"title"`
	Level     string `json:"level"`
	CreatedAt string `json:"created_at"`
}

type HomeStats struct {
	TotalUsers int64 `json:"total_users"`
}

type HomeResponse struct {
	QuickActions       []QuickActionVO `json:"quick_actions"`
	AvailableResources []QuickActionVO `json:"available_resources"`
	Notices            []HomeNotice    `json:"notices"`
	Stats              HomeStats       `json:"stats"`
}

type AddQuickActionReq struct {
	ResourceID string `json:"resource_id" binding:"required"`
}

type RemoveQuickActionReq struct {
	ID string `json:"id" binding:"required"`
}

type SortQuickActionReq struct {
	IDs []string `json:"ids" binding:"required"`
}

func GetHomeData(userID string) (*HomeResponse, error) {
	ctx := context.Background()

	quickActions, err := getQuickActions(ctx, userID)
	if err != nil {
		return nil, err
	}

	availableResources, err := getAvailableResources(ctx, userID)
	if err != nil {
		return nil, err
	}

	notices, err := getNotices(ctx)
	if err != nil {
		return nil, err
	}

	stats, err := getStats(ctx)
	if err != nil {
		return nil, err
	}

	return &HomeResponse{
		QuickActions:       quickActions,
		AvailableResources: availableResources,
		Notices:            notices,
		Stats:              *stats,
	}, nil
}

func getQuickActions(ctx context.Context, userID string) ([]QuickActionVO, error) {
	if userID == "" {
		return []QuickActionVO{}, nil
	}

	actions, err := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserIDEQ(userID)).
		Order(ent.Asc(sysquickaction.FieldSortCode), ent.Asc(sysquickaction.FieldCreatedAt)).
		All(ctx)
	if err != nil {
		return nil, err
	}

	// Resolve resource names
	resourceIDs := make([]string, len(actions))
	for i, a := range actions {
		resourceIDs[i] = a.ResourceID
	}

	resources, _ := db.Client.SysResource.Query().
		Where(sysresource.IDIn(resourceIDs...)).
		Select(sysresource.FieldID, sysresource.FieldName, sysresource.FieldIcon, sysresource.FieldPath).
		All(ctx)

	resMap := make(map[string]*ent.SysResource)
	for _, r := range resources {
		resMap[r.ID] = r
	}

	result := make([]QuickActionVO, 0, len(actions))
	for _, a := range actions {
		vo := QuickActionVO{
			ID:         a.ID,
			ResourceID: a.ResourceID,
			SortCode:   a.SortCode,
		}
		if r, ok := resMap[a.ResourceID]; ok {
			vo.Name = r.Name
			vo.Icon = r.Icon
			vo.RoutePath = r.Path
		}
		result = append(result, vo)
	}
	return result, nil
}

func getAvailableResources(ctx context.Context, userID string) ([]QuickActionVO, error) {
	if userID == "" {
		return []QuickActionVO{}, nil
	}

	// Get resource IDs already in quick actions
	existingIDs, _ := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserIDEQ(userID)).
		Select(sysquickaction.FieldResourceID).
		All(ctx)

	excludeMap := make(map[string]bool)
	for _, e := range existingIDs {
		excludeMap[e.ResourceID] = true
	}

	// Query available resources not in quick actions
	resources, err := db.Client.SysResource.Query().
		Where(
			sysresource.StatusEQ("ENABLED"),
			sysresource.TypeIn("MENU", "DIRECTORY"),
		).
		Order(ent.Asc(sysresource.FieldSortCode)).
		Limit(50).
		All(ctx)
	if err != nil {
		return nil, err
	}

	result := make([]QuickActionVO, 0, len(resources))
	for _, r := range resources {
		if excludeMap[r.ID] {
			continue
		}
		result = append(result, QuickActionVO{
			ResourceID: r.ID,
			ParentID:   r.ParentID,
			Type:       r.Type,
			Name:       r.Name,
			Icon:       r.Icon,
			RoutePath:  r.Path,
		})
	}
	return result, nil
}

func getNotices(ctx context.Context) ([]HomeNotice, error) {
	notices, err := db.Client.SysNotice.Query().
		Where(sysnotice.StatusEQ("ENABLED")).
		Order(ent.Desc(sysnotice.FieldCreatedAt)).
		Limit(5).
		All(ctx)
	if err != nil {
		return nil, err
	}

	result := make([]HomeNotice, len(notices))
	for i, n := range notices {
		level := n.Category
		if level == "" {
			level = "NORMAL"
		}
		result[i] = HomeNotice{
			ID:        n.ID,
			Title:     n.Title,
			Level:     level,
			CreatedAt: n.CreatedAt.Format("2006-01-02 15:04:05"),
		}
	}
	return result, nil
}

func getStats(ctx context.Context) (*HomeStats, error) {
	total, err := db.Client.SysUser.Query().Count(ctx)
	if err != nil {
		return nil, err
	}
	return &HomeStats{TotalUsers: int64(total)}, nil
}

func AddQuickAction(userID, resourceID string) error {
	if userID == "" {
		return nil
	}

	ctx := context.Background()

	// Check if already exists
	exists, err := db.Client.SysQuickAction.Query().
		Where(
			sysquickaction.UserIDEQ(userID),
			sysquickaction.ResourceIDEQ(resourceID),
		).
		Exist(ctx)
	if err != nil {
		return err
	}
	if exists {
		return nil
	}

	// Get current count for sort_code
	actionCount, err := db.Client.SysQuickAction.Query().
		Where(sysquickaction.UserIDEQ(userID)).
		Count(ctx)
	if err != nil {
		return err
	}

	now := time.Now()
	_, err = db.Client.SysQuickAction.Create().
		SetID(utils.NextID()).
		SetUserID(userID).
		SetResourceID(resourceID).
		SetSortCode((actionCount + 1) * 10).
		SetCreatedAt(now).
		SetCreatedBy(userID).
		Save(ctx)
	return err
}

func RemoveQuickAction(userID, actionID string) error {
	if userID == "" {
		return nil
	}

	ctx := context.Background()
	_, err := db.Client.SysQuickAction.Delete().
		Where(
			sysquickaction.IDEQ(actionID),
			sysquickaction.UserIDEQ(userID),
		).
		Exec(ctx)
	return err
}

func SortQuickActions(userID string, ids []string) error {
	if userID == "" {
		return nil
	}

	ctx := context.Background()
	for idx, id := range ids {
		_, err := db.Client.SysQuickAction.Update().
			Where(
				sysquickaction.IDEQ(id),
				sysquickaction.UserIDEQ(userID),
			).
			SetSortCode((idx + 1) * 10).
			Save(ctx)
		if err != nil {
			return err
		}
	}
	return nil
}
