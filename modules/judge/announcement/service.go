package announcement

import (
	"context"
	"time"


	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

func List(c *gin.Context, contestID string) gin.H {
	ctx := context.Background()
	var announcements []JudgeContestAnnouncement
	db.DB.WithContext(ctx).Where("contest_id = ?", contestID).Order("pinned DESC, created_at DESC").Find(&announcements)
	return gin.H{"code": 200, "data": announcements, "success": true}
}

func Create(c *gin.Context, contestID, title, content string, pinned bool, userID string) {
	ctx := context.Background()
	now := time.Now()
	entity := JudgeContestAnnouncement{
		ID:        utils.GenerateID(),
		ContestID: contestID,
		Title:     title,
		Content:   content,
		Pinned:    pinned,
		CreatedAt: &now,
		UpdatedAt: &now,
	}
	if userID != "" {
		entity.CreatedBy = &userID
		entity.UpdatedBy = &userID
	}
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("创建公告失败: "+err.Error(), 500))
	}
}

func Modify(c *gin.Context, id, title, content string, pinned bool, userID string) {
	ctx := context.Background()
	var entity JudgeContestAnnouncement
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		panic(exception.NewBusinessError("公告不存在", 404))
	}
	now := time.Now()
	updates := map[string]interface{}{
		"title": title, "content": content, "pinned": pinned, "updated_at": now,
	}
	if userID != "" {
		updates["updated_by"] = userID
	}
	if err := db.DB.WithContext(ctx).Model(&JudgeContestAnnouncement{}).Where("id = ?", id).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("编辑公告失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, id string) {
	db.DB.WithContext(context.Background()).Where("id = ?", id).Delete(&JudgeContestAnnouncement{})
}
