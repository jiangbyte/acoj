package config

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)



func Page(c *gin.Context, param *ConfigPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 { param.Current = 1 }
	if param.Size < 1 { param.Size = 10 }
	if param.Size > 100 { param.Size = 100 }

	query := db.DB.WithContext(ctx).Model(&SysConfig{})
	if param.Keyword != "" { query = query.Where("config_key LIKE ? OR remark LIKE ?", "%"+param.Keyword+"%", "%"+param.Keyword+"%") }
	if param.Category != "" { query = query.Where("category = ?", param.Category) }

	var total int64
	query.Count(&total)

	var records []SysConfig
	query.Order("sort_code ASC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)
	return result.PageDataResult(c, toVOList(records), total, param.Current, param.Size)
}

func Detail(c *gin.Context, id string) *ConfigVO {
	if id == "" { return nil }
	ctx := context.Background()
	var entity SysConfig
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound { return nil }
		panic(exception.NewBusinessError("查询配置详情失败: "+err.Error(), 500))
	}
	return toVO(&entity)
}

func Create(c *gin.Context, vo *ConfigVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	entity := SysConfig{
		ID: utils.GenerateID(), SortCode: vo.SortCode, CreatedAt: &now, UpdatedAt: &now,
	}
	if vo.ConfigKey != nil { entity.ConfigKey = vo.ConfigKey }
	if vo.ConfigValue != nil { entity.ConfigValue = vo.ConfigValue }
	if vo.Category != nil { entity.Category = vo.Category }
	if vo.Remark != nil { entity.Remark = vo.Remark }
	if vo.Extra != nil { entity.Extra = vo.Extra }
	if userID != "" { entity.CreatedBy = &userID; entity.UpdatedBy = &userID }

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加配置失败: "+err.Error(), 500))
	}
}

func Modify(c *gin.Context, vo *ConfigVO, userID string) {
	ctx := context.Background()
	var entity SysConfig
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound { panic(exception.NewBusinessError("配置不存在", 400)) }
		panic(exception.NewBusinessError("查询配置失败: "+err.Error(), 500))
	}

	updates := map[string]interface{}{"sort_code": vo.SortCode, "updated_at": time.Now()}
	if vo.ConfigKey != nil { updates["config_key"] = *vo.ConfigKey }
	if vo.ConfigValue != nil { updates["config_value"] = *vo.ConfigValue }
	if vo.Category != nil { updates["category"] = *vo.Category }
	if vo.Remark != nil { updates["remark"] = *vo.Remark }
	if vo.Extra != nil { updates["extra"] = *vo.Extra }
	if userID != "" { updates["updated_by"] = userID }

	if err := db.DB.WithContext(ctx).Model(&SysConfig{}).Where("id = ?", vo.ID).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("编辑配置失败: "+err.Error(), 500))
	}
}

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 { return }
	ctx := context.Background()
	if err := db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&SysConfig{}).Error; err != nil {
		panic(exception.NewBusinessError("删除配置失败: "+err.Error(), 500))
	}
}

func ListByCategory(c *gin.Context, category string) []ConfigVO {
	ctx := context.Background()
	var records []SysConfig
	db.DB.WithContext(ctx).Model(&SysConfig{}).Where("category = ?", category).Order("sort_code ASC").Find(&records)
	return toVOList(records)
}

func EditBatch(c *gin.Context, param *ConfigBatchEditParam, userID string) {
	ctx := context.Background()
	now := time.Now()
	tx := db.DB.WithContext(ctx).Begin()
	for _, item := range param.Configs {
		up := map[string]interface{}{"updated_at": now}
		if item.ConfigKey != nil { up["config_key"] = *item.ConfigKey }
		if item.ConfigValue != nil { up["config_value"] = *item.ConfigValue }
		if item.Remark != nil { up["remark"] = *item.Remark }
		if item.SortCode != 0 { up["sort_code"] = item.SortCode }
		if userID != "" { up["updated_by"] = userID }
		if err := tx.Model(&SysConfig{}).Where("id = ?", item.ID).Updates(up).Error; err != nil {
			tx.Rollback()
			panic(exception.NewBusinessError("批量编辑配置失败: "+err.Error(), 500))
		}
	}
	if err := tx.Commit().Error; err != nil {
		panic(exception.NewBusinessError("提交事务失败: "+err.Error(), 500))
	}
}

func EditByCategory(c *gin.Context, param *ConfigCategoryEditParam, userID string) {
	ctx := context.Background()
	now := time.Now()
	up := map[string]interface{}{"updated_at": now}
	if param.ConfigKey != nil { up["config_key"] = *param.ConfigKey }
	if param.ConfigValue != nil { up["config_value"] = *param.ConfigValue }
	if param.Remark != nil { up["remark"] = *param.Remark }
	if userID != "" { up["updated_by"] = userID }
	if err := db.DB.WithContext(ctx).Model(&SysConfig{}).Where("category = ?", param.Category).Updates(up).Error; err != nil {
		panic(exception.NewBusinessError("按分类编辑配置失败: "+err.Error(), 500))
	}
}
