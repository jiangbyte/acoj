package position

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"hei-gin/core/pojo"

	"github.com/gin-gonic/gin"
)



func formatTime(t *time.Time) string { if t == nil { return "" }; return pojo.FormatDateTime(*t) }
func entToVO(entity *SysPosition) *PositionVO {
	if entity == nil { return nil }
	return &PositionVO{
		ID: entity.ID, Code: entity.Code, Name: entity.Name, Category: entity.Category,
		Description: entity.Description, Status: entity.Status, SortCode: entity.SortCode,
		Extra: entity.Extra, CreatedAt: formatTime(entity.CreatedAt),
		CreatedBy: entity.CreatedBy, UpdatedAt: formatTime(entity.UpdatedAt),
		UpdatedBy: entity.UpdatedBy,
	}
}
func Page(c *gin.Context, param *PositionPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }
	if param.Size > 100 { param.Size = 100 }

	query := db.DB.WithContext(ctx).Model(&SysPosition{})
	if param.Keyword != "" { query = query.Where("name LIKE ?", "%"+param.Keyword+"%") }
	if param.Category != "" { query = query.Where("category = ?", param.Category) }
	if param.OrgID != "" { query = query.Where("org_id = ?", param.OrgID) }

	var total int64
	query.Count(&total)

	var records []SysPosition
	query.Order("sort_code ASC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)

	vos := make([]*PositionVO, len(records))
	for i, r := range records { vos[i] = entToVO(&r) }
	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *PositionVO {
	if id == "" { return nil }
	ctx := context.Background()
	var entity SysPosition
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询职位详情失败: "+err.Error(), 500))
	}
	vo := entToVO(&entity)
	return vo
}

func Create(c *gin.Context, vo *PositionVO, userID string) {
	ctx := context.Background()
	now := time.Now()
	entity := SysPosition{ID: utils.GenerateID(), Code: vo.Code, Name: vo.Name, Category: vo.Category, Status: vo.Status, SortCode: vo.SortCode, CreatedAt: &now, UpdatedAt: &now}
	if vo.OrgID != nil { entity.OrgID = vo.OrgID }
	if vo.GroupID != nil { entity.GroupID = vo.GroupID }
	if vo.Description != nil { entity.Description = vo.Description }
	if vo.Extra != nil { entity.Extra = vo.Extra }
	if userID != "" { entity.CreatedBy = &userID; entity.UpdatedBy = &userID }
	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil { panic(exception.NewBusinessError("添加职位失败: "+err.Error(), 500)) }
}

func Modify(c *gin.Context, vo *PositionVO, userID string) {
	ctx := context.Background()
	if vo.ID == "" { panic(exception.NewBusinessError("ID不能为空", 400)) }

	var entity SysPosition
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound { panic(exception.NewBusinessError("数据不存在", 400)) }
		panic(exception.NewBusinessError("查询职位失败: "+err.Error(), 500))
	}

	up := map[string]interface{}{"code": vo.Code, "name": vo.Name, "category": vo.Category, "status": vo.Status, "sort_code": vo.SortCode, "updated_at": time.Now()}
	if vo.OrgID != nil { up["org_id"] = *vo.OrgID } else { up["org_id"] = nil }
	if vo.GroupID != nil { up["group_id"] = *vo.GroupID } else { up["group_id"] = nil }
	if vo.Description != nil { up["description"] = *vo.Description }
	if vo.Extra != nil { up["extra"] = *vo.Extra }
	if userID != "" { up["updated_by"] = userID }

	if err := db.DB.WithContext(ctx).Model(&SysPosition{}).Where("id = ?", vo.ID).Updates(up).Error; err != nil { panic(exception.NewBusinessError("编辑职位失败: "+err.Error(), 500)) }
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()
	db.DB.WithContext(ctx).Table("sys_user").Where("position_id IN ?", ids).Update("position_id", nil)
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&SysPosition{})
}

func Options(c *gin.Context) []*PositionVO {
	ctx := context.Background()
	var records []SysPosition
	db.DB.WithContext(ctx).Order("sort_code ASC").Find(&records)
	vos := make([]*PositionVO, len(records))
	for i, r := range records { vos[i] = entToVO(&r) }
	return vos
}

