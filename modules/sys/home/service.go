package home

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
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

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT q.id, q.resource_id, q.sort_code,
		        COALESCE(r.name,''), COALESCE(r.icon,''), COALESCE(r.path,'')
		 FROM sys_quick_action q
		 LEFT JOIN sys_resource r ON r.id = q.resource_id
		 WHERE q.user_id = ?
		 ORDER BY q.sort_code ASC, q.created_at ASC`, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []QuickActionVO
	for rows.Next() {
		var vo QuickActionVO
		if err := rows.Scan(&vo.ID, &vo.ResourceID, &vo.SortCode, &vo.Name, &vo.Icon, &vo.RoutePath); err != nil {
			return nil, err
		}
		result = append(result, vo)
	}
	return result, nil
}

func getAvailableResources(ctx context.Context, userID string) ([]QuickActionVO, error) {
	if userID == "" {
		return []QuickActionVO{}, nil
	}

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT r.id, COALESCE(r.parent_id,''), COALESCE(r.type,''),
		        COALESCE(r.name,''), COALESCE(r.icon,''), COALESCE(r.path,'')
		 FROM sys_resource r
		 WHERE r.status = 'ENABLED'
		   AND r.type IN ('MENU', 'DIRECTORY')
		   AND r.id NOT IN (SELECT resource_id FROM sys_quick_action WHERE user_id = ?)
		 ORDER BY r.sort_code ASC
		 LIMIT 50`, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []QuickActionVO
	for rows.Next() {
		var vo QuickActionVO
		if err := rows.Scan(&vo.ResourceID, &vo.ParentID, &vo.Type, &vo.Name, &vo.Icon, &vo.RoutePath); err != nil {
			return nil, err
		}
		result = append(result, vo)
	}
	return result, nil
}

func getNotices(ctx context.Context) ([]HomeNotice, error) {
	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT id, title, COALESCE(category,'NORMAL'), created_at
		 FROM sys_notice
		 WHERE status = 'ENABLED'
		 ORDER BY created_at DESC
		 LIMIT 5`)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []HomeNotice
	for rows.Next() {
		var notice HomeNotice
		var createdAt time.Time
		if err := rows.Scan(&notice.ID, &notice.Title, &notice.Level, &createdAt); err != nil {
			return nil, err
		}
		notice.CreatedAt = createdAt.Format("2006-01-02 15:04:05")
		result = append(result, notice)
	}
	return result, nil
}

func getStats(ctx context.Context) (*HomeStats, error) {
	var total int64
	err := db.RawDB.QueryRowContext(ctx, "SELECT COUNT(*) FROM sys_user").Scan(&total)
	if err != nil {
		return nil, err
	}
	return &HomeStats{TotalUsers: total}, nil
}

func AddQuickAction(userID, resourceID string) error {
	if userID == "" {
		return nil
	}

	ctx := context.Background()

	// Check if already exists
	var count int
	err := db.RawDB.QueryRowContext(ctx,
		"SELECT COUNT(*) FROM sys_quick_action WHERE user_id = ? AND resource_id = ?",
		userID, resourceID).Scan(&count)
	if err != nil {
		return err
	}
	if count > 0 {
		return nil
	}

	// Get current count for sort_code
	var actionCount int
	db.RawDB.QueryRowContext(ctx,
		"SELECT COUNT(*) FROM sys_quick_action WHERE user_id = ?", userID).Scan(&actionCount)

	now := time.Now()
	_, err = db.RawDB.ExecContext(ctx,
		`INSERT INTO sys_quick_action (id, user_id, resource_id, sort_code, created_at, created_by)
		 VALUES (?, ?, ?, ?, ?, ?)`,
		utils.NextID(), userID, resourceID, (actionCount+1)*10, now, userID)
	return err
}

func RemoveQuickAction(userID, actionID string) error {
	if userID == "" {
		return nil
	}

	ctx := context.Background()
	_, err := db.RawDB.ExecContext(ctx,
		"DELETE FROM sys_quick_action WHERE id = ? AND user_id = ?",
		actionID, userID)
	return err
}

func SortQuickActions(userID string, ids []string) error {
	if userID == "" {
		return nil
	}

	ctx := context.Background()
	for idx, id := range ids {
		_, err := db.RawDB.ExecContext(ctx,
			"UPDATE sys_quick_action SET sort_code = ? WHERE id = ? AND user_id = ?",
			(idx+1)*10, id, userID)
		if err != nil {
			return err
		}
	}
	return nil
}
