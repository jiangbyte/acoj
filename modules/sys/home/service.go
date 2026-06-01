package home

import (
	"context"
	"time"

	"hei-gin/core/auth"
	"hei-gin/core/db"

	"github.com/gin-gonic/gin"
	"hei-gin/core/utils"
	resModel "hei-gin/modules/sys/resource"
)

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
func HomeAddQuickAction(c *gin.Context, param *AddQuickActionParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return
	}
	ctx := context.Background()
	var count int64
	db.DB.WithContext(ctx).Model(&SysQuickAction{}).Where("user_id = ? AND resource_id = ?", userID, param.ResourceID).Count(&count)
	if count > 0 {
		return
	}
	var actionCount int64
	db.DB.WithContext(ctx).Model(&SysQuickAction{}).Where("user_id = ?", userID).Count(&actionCount)
	now := time.Now()
	entity := SysQuickAction{
		ID: utils.GenerateID(), UserID: userID, ResourceID: param.ResourceID,
		SortCode: int(actionCount+1) * 10, CreatedAt: &now, UpdatedAt: &now,
	}
	entity.CreatedBy = &userID
	entity.UpdatedBy = &userID
	db.DB.WithContext(ctx).Create(&entity)
}
func HomeRemoveQuickAction(c *gin.Context, param *RemoveQuickActionParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Delete(&SysQuickAction{}, "id = ?", param.ID)
}
func HomeSortQuickActions(c *gin.Context, param *SortQuickActionParam) {
	userID := auth.GetLoginIDDefaultNull(c)
	if userID == "" {
		return
	}
	ctx := context.Background()
	for idx, id := range param.IDs {
		db.DB.WithContext(ctx).Model(&SysQuickAction{}).Where("id = ?", id).Updates(map[string]interface{}{
			"sort_code": (idx + 1) * 10,
		})
	}
}
func findQuickActionsByUserID(userID string) []QuickActionVO {
	ctx := context.Background()
	var actions []SysQuickAction
	db.DB.WithContext(ctx).Where("user_id = ?", userID).Order("sort_code ASC, created_at ASC").Find(&actions)
	if len(actions) == 0 {
		return make([]QuickActionVO, 0)
	}
	resourceIDs := make([]string, len(actions))
	for i, a := range actions {
		resourceIDs[i] = a.ResourceID
	}
	var resources []resModel.SysResource
	db.DB.WithContext(ctx).Where("id IN ?", resourceIDs).Find(&resources)
	resourceMap := make(map[string]resModel.SysResource)
	for _, r := range resources {
		resourceMap[r.ID] = r
	}
	vos := make([]QuickActionVO, 0, len(actions))
	for _, a := range actions {
		vo := QuickActionVO{ID: a.ID, ResourceID: a.ResourceID, SortCode: a.SortCode}
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
func getAvailableResources(userID string) []QuickActionVO {
	ctx := context.Background()
	var actionIDs []string
	db.DB.WithContext(ctx).Model(&SysQuickAction{}).Where("user_id = ?", userID).Select("resource_id").Find(&actionIDs)
	var resources []resModel.SysResource
	query := db.DB.WithContext(ctx).Model(&resModel.SysResource{}).Where("category IN ? AND status = ?", []string{"BACKEND_MENU", "FRONTEND_MENU"}, "ENABLED")
	if len(actionIDs) > 0 {
		query = query.Where("id NOT IN ?", actionIDs)
	}
	query.Order("sort_code ASC").Find(&resources)
	vos := make([]QuickActionVO, len(resources))
	for i, r := range resources {
		vos[i] = QuickActionVO{
			ResourceID: r.ID, Name: r.Name, Type: r.Type,
		}
		if r.Icon != nil {
			vos[i].Icon = *r.Icon
		}
		if r.RoutePath != nil {
			vos[i].RoutePath = *r.RoutePath
		}
		if r.ParentID != nil {
			vos[i].ParentID = *r.ParentID
		}
	}
	return vos
}
func getNotices() []HomeNotice {
	ctx := context.Background()
	type noticeRow struct {
		ID        string
		Title     string
		Level     string
		CreatedAt *time.Time
	}
	var rows []noticeRow
	db.DB.WithContext(ctx).Table("sys_notice").
		Where("status = ?", "ENABLED").
		Where("category = ?", "PLATFORM").
		Order("sort_code ASC, is_top DESC").
		Select("id, title, level, created_at").
		Limit(5).
		Find(&rows)
	results := make([]HomeNotice, len(rows))
	for i, r := range rows {
		notice := HomeNotice{ID: r.ID, Title: r.Title, Level: r.Level}
		if r.CreatedAt != nil {
			notice.CreatedAt = r.CreatedAt.Format("2006-01-02 15:04:05")
		}
		results[i] = notice
	}
	return results
}
func getUserCount() int {
	ctx := context.Background()
	// This references the user model
	type _user struct{}
	_ = _user{}
	var count int64
	db.DB.WithContext(ctx).Table("sys_user").Count(&count)
	return int(count)
}
