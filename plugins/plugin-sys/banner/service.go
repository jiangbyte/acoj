package banner

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/sdk/db"
	"hei-gin/sdk/exception"
	"hei-gin/sdk/result"
	"hei-gin/sdk/utils"

	"github.com/gin-gonic/gin"
)

func Page(c *gin.Context, param *BannerPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }

	offset := (param.Current - 1) * param.Size

	var total int64
	db.DB.WithContext(ctx).Model(&SysBanner{}).Count(&total)

	var records []SysBanner
	db.DB.WithContext(ctx).Order("created_at DESC").Limit(param.Size).Offset(offset).Find(&records)

	vos := make([]*BannerVO, 0, len(records))
	for _, r := range records { vos = append(vos, entToVO(&r)) }
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *BannerVO {
	if id == "" { return nil }
	ctx := context.Background()
	var entity SysBanner
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询Banner详情失败: "+err.Error(), 500))
	}
	return entToVO(&entity)
}

func Create(c *gin.Context, vo *BannerVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	entity := SysBanner{
		ID: utils.GenerateID(), Title: vo.Title, Image: vo.Image,
		Category: vo.Category, Type: vo.Type, Position: vo.Position,
		SortCode: vo.SortCode, ViewCount: vo.ViewCount, ClickCount: vo.ClickCount,
		CreatedAt: &now, UpdatedAt: &now,
	}
	if vo.LinkType != "" { entity.LinkType = vo.LinkType }
	if vo.URL != nil { entity.URL = vo.URL }
	if vo.Summary != nil { entity.Summary = vo.Summary }
	if vo.Description != nil { entity.Description = vo.Description }
	if userID != "" { entity.CreatedBy = &userID; entity.UpdatedBy = &userID }

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加Banner失败: "+err.Error(), 500))
	}
}

func Modify(c *gin.Context, vo *BannerVO, userID string) {
	ctx := context.Background()
	if vo.ID == "" { panic(exception.NewBusinessError("ID不能为空", 400)) }

	var entity SysBanner
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound { panic(exception.NewBusinessError("Banner不存在", 404)) }
		panic(exception.NewBusinessError("查询Banner失败: "+err.Error(), 500))
	}

	now := time.Now()
	updates := map[string]interface{}{
		"title": vo.Title, "image": vo.Image, "category": vo.Category,
		"type": vo.Type, "position": vo.Position, "sort_code": vo.SortCode,
		"view_count": vo.ViewCount, "click_count": vo.ClickCount, "updated_at": now,
	}
	if vo.LinkType != "" { updates["link_type"] = vo.LinkType }
	if vo.URL != nil { updates["url"] = *vo.URL }
	if vo.Summary != nil { updates["summary"] = *vo.Summary }
	if vo.Description != nil { updates["description"] = *vo.Description }
	if userID != "" { updates["updated_by"] = userID }

	if err := db.DB.WithContext(ctx).Model(&SysBanner{}).Where("id = ?", vo.ID).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("编辑Banner失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&SysBanner{})
}

func entToVO(entity *SysBanner) *BannerVO {
	vo := &BannerVO{
		ID: entity.ID, Title: entity.Title, Image: entity.Image,
		LinkType: entity.LinkType, Category: entity.Category,
		Type: entity.Type, Position: entity.Position,
		SortCode: entity.SortCode, ViewCount: entity.ViewCount, ClickCount: entity.ClickCount,
	}
	if entity.URL != nil { vo.URL = entity.URL }
	if entity.Summary != nil { vo.Summary = entity.Summary }
	if entity.Description != nil { vo.Description = entity.Description }
	if entity.CreatedAt != nil { s := entity.CreatedAt.Format("2006-01-02 15:04:05"); vo.CreatedAt = &s }
	if entity.CreatedBy != nil { vo.CreatedBy = entity.CreatedBy }
	if entity.UpdatedAt != nil { s := entity.UpdatedAt.Format("2006-01-02 15:04:05"); vo.UpdatedAt = &s }
	if entity.UpdatedBy != nil { vo.UpdatedBy = entity.UpdatedBy }
	return vo
}
