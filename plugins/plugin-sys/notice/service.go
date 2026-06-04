package notice

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

func parseTime(s *string) *time.Time {
	if s == nil || *s == "" { return nil }
	t, err := time.Parse("2006-01-02 15:04:05", *s)
	if err != nil { return nil }
	return &t
}

func Page(c *gin.Context, param *NoticePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }

	query := db.DB.WithContext(ctx).Model(&SysNotice{})
	if param.Keyword != "" { query = query.Where("title LIKE ?", "%"+param.Keyword+"%") }
	if param.Category != "" { query = query.Where("category = ?", param.Category) }
	if param.Status != "" { query = query.Where("status = ?", param.Status) }

	var total int64
	query.Count(&total)

	var records []SysNotice
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)

	vos := make([]*NoticeVO, len(records))
	for i, r := range records { vos[i] = entToVO(&r) }
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *NoticeVO {
	if id == "" { return nil }
	ctx := context.Background()
	var entity SysNotice
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询通知详情失败: "+err.Error(), 500))
	}
	return entToVO(&entity)
}

func Create(c *gin.Context, vo *NoticeVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	entity := SysNotice{
		ID: utils.GenerateID(), Title: vo.Title, Category: vo.Category, Type: vo.Type,
		SortCode: vo.SortCode, CreatedAt: &now, UpdatedAt: &now,
	}
	if vo.Summary != nil { entity.Summary = vo.Summary }
	if vo.Content != nil { entity.Content = vo.Content }
	if vo.Cover != nil { entity.Cover = vo.Cover }
	if vo.Level != "" { entity.Level = vo.Level }
	if vo.Status != "" { entity.Status = vo.Status }
	if vo.IsTop != "" { entity.IsTop = vo.IsTop }
	if vo.Author != nil { entity.Author = vo.Author }
	if vo.PublishAt != nil { entity.PublishAt = parseTime(vo.PublishAt) }
	if vo.ExpireAt != nil { entity.ExpireAt = parseTime(vo.ExpireAt) }
	if userID != "" { entity.CreatedBy = &userID; entity.UpdatedBy = &userID }

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加通知失败: "+err.Error(), 500))
	}
}

func Modify(c *gin.Context, vo *NoticeVO, userID string) {
	ctx := context.Background()
	if vo.ID == "" { panic(exception.NewBusinessError("ID不能为空", 400)) }

	var entity SysNotice
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound { panic(exception.NewBusinessError("通知不存在", 404)) }
		panic(exception.NewBusinessError("查询通知失败: "+err.Error(), 500))
	}

	up := map[string]interface{}{
		"title": vo.Title, "category": vo.Category, "type": vo.Type,
		"sort_code": vo.SortCode, "updated_at": time.Now(),
	}
	if vo.Summary != nil { up["summary"] = *vo.Summary } else { up["summary"] = nil }
	if vo.Content != nil { up["content"] = *vo.Content } else { up["content"] = nil }
	if vo.Cover != nil { up["cover"] = *vo.Cover } else { up["cover"] = nil }
	if vo.Level != "" { up["level"] = vo.Level }
	if vo.Status != "" { up["status"] = vo.Status }
	if vo.IsTop != "" { up["is_top"] = vo.IsTop } else { up["is_top"] = nil }
	if vo.Author != nil { up["author"] = *vo.Author } else { up["author"] = nil }
	if vo.PublishAt != nil { up["publish_at"] = parseTime(vo.PublishAt) } else { up["publish_at"] = nil }
	if vo.ExpireAt != nil { up["expire_at"] = parseTime(vo.ExpireAt) } else { up["expire_at"] = nil }
	if userID != "" { up["updated_by"] = userID }

	if err := db.DB.WithContext(ctx).Model(&SysNotice{}).Where("id = ?", vo.ID).Updates(up).Error; err != nil {
		panic(exception.NewBusinessError("编辑通知失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	db.DB.WithContext(context.Background()).Where("id IN ?", ids).Delete(&SysNotice{})
}

func PublicPage(c *gin.Context, param *NoticePageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 || param.Size > 100 {
		param.Size = 10
	}

	q := db.DB.WithContext(ctx).Model(&SysNotice{}).Where("status = ?", "ENABLED")
	if param.Keyword != "" {
		q = q.Where("title LIKE ?", "%"+param.Keyword+"%")
	}
	if param.Category != "" {
		q = q.Where("category = ?", param.Category)
	}

	var total int64
	q.Count(&total)

	var records []SysNotice
	q.Order("is_top DESC, sort_code DESC, created_at DESC").Limit(param.Size).Offset((param.Current-1)*param.Size).Find(&records)

	vos := make([]*NoticeVO, len(records))
	for i, r := range records {
		vos[i] = entToVO(&r)
	}
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func PublicDetail(c *gin.Context, id string) *NoticeVO {
	if id == "" {
		return nil
	}
	ctx := context.Background()
	var entity SysNotice
	if err := db.DB.WithContext(ctx).Where("id = ? AND status = ?", id, "ENABLED").First(&entity).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询通知详情失败: "+err.Error(), 500))
	}
	return entToVO(&entity)
}

func Latest(c *gin.Context, param *NoticeLatestParam) []*NoticeVO {
	ctx := context.Background()
	var records []SysNotice
	db.DB.WithContext(ctx).
		Where("status = ?", "ENABLED").
		Order("is_top DESC, sort_code DESC, created_at DESC").
		Limit(param.Size).
		Find(&records)
	vos := make([]*NoticeVO, len(records))
	for i, r := range records {
		vos[i] = entToVO(&r)
	}
	return vos
}

func entToVO(entity *SysNotice) *NoticeVO {
	vo := &NoticeVO{
		ID: entity.ID, Title: entity.Title, Category: entity.Category,
		Type: entity.Type, SortCode: entity.SortCode,
	}
	if entity.Summary != nil { vo.Summary = entity.Summary }
	if entity.Content != nil { vo.Content = entity.Content }
	if entity.Cover != nil { vo.Cover = entity.Cover }
	if entity.Level != "" { vo.Level = entity.Level }
	if entity.Status != "" { vo.Status = entity.Status }
	if entity.IsTop != "" { vo.IsTop = entity.IsTop }
	if entity.Author != nil { vo.Author = entity.Author }
	if entity.PublishAt != nil { s := entity.PublishAt.Format("2006-01-02 15:04:05"); vo.PublishAt = &s }
	if entity.ExpireAt != nil { s := entity.ExpireAt.Format("2006-01-02 15:04:05"); vo.ExpireAt = &s }
	if entity.CreatedAt != nil { vo.CreatedAt = entity.CreatedAt.Format("2006-01-02 15:04:05") }
	if entity.CreatedBy != nil { vo.CreatedBy = entity.CreatedBy }
	if entity.UpdatedAt != nil { vo.UpdatedAt = entity.UpdatedAt.Format("2006-01-02 15:04:05") }
	if entity.UpdatedBy != nil { vo.UpdatedBy = entity.UpdatedBy }
	return vo
}
