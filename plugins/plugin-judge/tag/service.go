package tag

import (
	"context"
	"time"

	"hei-gin/sdk/db"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"github.com/gin-gonic/gin"
)

// PageService 标签分页
func PageService(c *gin.Context, param *TagPageParam) gin.H {
	ctx := context.Background()
	tx := db.DB.WithContext(ctx).Model(&JudgeTag{})

	if param.Keyword != "" {
		tx = tx.Where("name LIKE ?", "%"+param.Keyword+"%")
	}

	var total int64
	tx.Count(&total)

	page := param.Current
	size := param.Size
	if page < 1 {
		page = 1
	}
	if size < 1 || size > 100 {
		size = 10
	}

	var tags []JudgeTag
	tx.Offset((page - 1) * size).Limit(size).Order("created_at DESC").Find(&tags)

	voList := make([]TagVO, len(tags))
	for i, t := range tags {
		voList[i] = modelToVO(&t)
	}

	return result.PageDataResult(c, voList, total, page, size)
}

// CreateService 创建标签
func CreateService(c *gin.Context, param *TagCreateParam) error {
	ctx := context.Background()
	now := time.Now()

	tag := JudgeTag{
		ID:        utils.GenerateID(),
		Name:      param.Name,
		Color:     param.Color,
		CreatedAt: &now,
		UpdatedAt: &now,
	}

	return db.DB.WithContext(ctx).Create(&tag).Error
}

// ModifyService 编辑标签
func ModifyService(c *gin.Context, param *TagModifyParam) error {
	ctx := context.Background()
	updates := map[string]any{}
	if param.Name != "" {
		updates["name"] = param.Name
	}
	if param.Color != "" {
		updates["color"] = param.Color
	}
	updates["updated_at"] = time.Now()

	return db.DB.WithContext(ctx).Model(&JudgeTag{}).Where("id = ?", param.ID).Updates(updates).Error
}

// RemoveService 删除标签
func RemoveService(c *gin.Context, param TagRemoveParam) error {
	ctx := context.Background()
	return db.DB.WithContext(ctx).Where("id IN ?", param.IDs).Delete(&JudgeTag{}).Error
}

// ListAllService 获取所有标签（无分页）
func ListAllService(c *gin.Context) ([]TagVO, error) {
	ctx := context.Background()
	var tags []JudgeTag
	if err := db.DB.WithContext(ctx).Order("name ASC").Find(&tags).Error; err != nil {
		return nil, err
	}
	voList := make([]TagVO, len(tags))
	for i, t := range tags {
		voList[i] = modelToVO(&t)
	}
	return voList, nil
}

func modelToVO(t *JudgeTag) TagVO {
	createdAt := ""
	if t.CreatedAt != nil {
		createdAt = t.CreatedAt.Format("2006-01-02 15:04:05")
	}
	return TagVO{
		ID:        t.ID,
		Name:      t.Name,
		Color:     t.Color,
		CreatedAt: createdAt,
	}
}
