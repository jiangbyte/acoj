package config

import (
	"context"
	"encoding/json"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/predicate"
	"hei-gin/ent/gen/sysconfig"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
)

// Page returns a paginated list of configs.
func Page(c *gin.Context, param *ConfigPageParam) gin.H {
	ctx := context.Background()

	// Set defaults
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	offset := (param.Current - 1) * param.Size

	// Build query conditions
	var conds []predicate.SysConfig
	if param.Category != "" {
		conds = append(conds, sysconfig.CategoryEQ(param.Category))
	}
	if param.Keyword != "" {
		conds = append(conds, sysconfig.ConfigKeyContains(param.Keyword))
	}

	// Count query
	total, err := db.Client.SysConfig.Query().Where(conds...).Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询配置列表失败: "+err.Error(), 500))
	}

	// Data query with ordering and pagination
	records, err := db.Client.SysConfig.Query().
		Where(conds...).
		Order(sysconfig.BySortCode(sql.OrderAsc())).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询配置列表失败: "+err.Error(), 500))
	}

	vos := make([]*ConfigVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(r))
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// ListByCategory returns configs filtered by category.
func ListByCategory(c *gin.Context, category string) []*ConfigVO {
	ctx := context.Background()

	records, err := db.Client.SysConfig.Query().
		Where(sysconfig.CategoryEQ(category)).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询配置列表失败: "+err.Error(), 500))
	}

	vos := make([]*ConfigVO, 0, len(records))
	for _, r := range records {
		vos = append(vos, entToVO(r))
	}

	return vos
}

// Create creates a new config.
func Create(c *gin.Context, vo *ConfigVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	builder := db.Client.SysConfig.Create().
		SetID(utils.GenerateID()).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if vo.ConfigKey != "" {
		builder.SetConfigKey(vo.ConfigKey)
	}
	if vo.ConfigValue != "" {
		builder.SetConfigValue(vo.ConfigValue)
	}
	if vo.Category != "" {
		builder.SetCategory(vo.Category)
	}
	if vo.Remark != nil {
		builder.SetNillableRemark(vo.Remark)
	}
	if vo.SortCode != 0 {
		builder.SetSortCode(vo.SortCode)
	}
	if vo.Extra != nil {
		builder.SetNillableExtra(vo.Extra)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加配置失败: "+err.Error(), 500))
	}
}

// Modify updates an existing config.
func Modify(c *gin.Context, vo *ConfigVO, userID string) {
	ctx := context.Background()

	if vo.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify the config exists
	entity, err := db.Client.SysConfig.Get(ctx, vo.ID)
	if err != nil {
		if ent.IsNotFound(err) {
			panic(exception.NewBusinessError("数据不存在", 404))
		}
		panic(exception.NewBusinessError("查询配置失败: "+err.Error(), 500))
	}

	now := time.Now()
	builder := db.Client.SysConfig.UpdateOneID(vo.ID).
		SetUpdatedAt(now)

	if vo.ConfigKey != "" {
		builder.SetConfigKey(vo.ConfigKey)
	}
	if vo.ConfigValue != "" {
		builder.SetConfigValue(vo.ConfigValue)
	}
	if vo.Category != "" {
		builder.SetCategory(vo.Category)
	}
	if vo.Remark != nil {
		builder.SetNillableRemark(vo.Remark)
	}
	if vo.SortCode != 0 {
		builder.SetSortCode(vo.SortCode)
	}
	if vo.Extra != nil {
		builder.SetNillableExtra(vo.Extra)
	}
	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑配置失败: "+err.Error(), 500))
	}

	// Clear Redis cache for the modified config key
	if entity.ConfigKey != nil {
		db.Redis.Del(ctx, "sys-config:"+*entity.ConfigKey)
	}
}

// Remove deletes configs by IDs.
func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}

	ctx := context.Background()

	// First query to get all config_keys for cache cleanup
	entities, err := db.Client.SysConfig.Query().
		Where(sysconfig.IDIn(ids...)).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询配置失败: "+err.Error(), 500))
	}

	// Delete from DB
	_, err = db.Client.SysConfig.Delete().Where(sysconfig.IDIn(ids...)).Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除配置失败: "+err.Error(), 500))
	}

	// Delete Redis cache for each key
	for _, entity := range entities {
		if entity.ConfigKey != nil {
			db.Redis.Del(ctx, "sys-config:"+*entity.ConfigKey)
		}
	}
}

// Detail returns a single config by ID.
func Detail(c *gin.Context, id string) *ConfigVO {
	if id == "" {
		return nil
	}

	ctx := context.Background()
	entity, err := db.Client.SysConfig.Get(ctx, id)
	if err != nil {
		if ent.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询配置详情失败: "+err.Error(), 500))
	}

	return entToVO(entity)
}

// EditBatch updates multiple configs in a batch.
func EditBatch(c *gin.Context, param *ConfigBatchEditParam, userID string) {
	if len(param.Configs) == 0 {
		return
	}

	ctx := context.Background()
	now := time.Now()

	// Collect all IDs
	ids := make([]string, 0, len(param.Configs))
	for _, vo := range param.Configs {
		if vo.ID != "" {
			ids = append(ids, vo.ID)
		}
	}

	// Query all existing configs by IDs
	entities, err := db.Client.SysConfig.Query().
		Where(sysconfig.IDIn(ids...)).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询配置失败: "+err.Error(), 500))
	}

	// Build entity map keyed by ID
	entityMap := make(map[string]*ent.SysConfig, len(entities))
	for _, e := range entities {
		entityMap[e.ID] = e
	}

	// Collect keys for cache cleanup
	var cacheKeys []string

	for _, vo := range param.Configs {
		entity, ok := entityMap[vo.ID]
		if !ok {
			panic(exception.NewBusinessError("配置不存在: "+vo.ID, 400))
		}

		builder := db.Client.SysConfig.UpdateOneID(vo.ID).
			SetUpdatedAt(now)

		if vo.ConfigKey != "" {
			builder.SetConfigKey(vo.ConfigKey)
		}
		if vo.ConfigValue != "" {
			builder.SetConfigValue(vo.ConfigValue)
		}
		if vo.Category != "" {
			builder.SetCategory(vo.Category)
		}
		if vo.Remark != nil {
			builder.SetNillableRemark(vo.Remark)
		}
		if vo.SortCode != 0 {
			builder.SetSortCode(vo.SortCode)
		}
		if vo.Extra != nil {
			builder.SetNillableExtra(vo.Extra)
		}
		if userID != "" {
			builder.SetUpdatedBy(userID)
		}

		_, err := builder.Save(ctx)
		if err != nil {
			panic(exception.NewBusinessError("编辑配置失败: "+err.Error(), 500))
		}

		if entity.ConfigKey != nil {
			cacheKeys = append(cacheKeys, "sys-config:"+*entity.ConfigKey)
		}
		if vo.ConfigKey != "" && (entity.ConfigKey == nil || *entity.ConfigKey != vo.ConfigKey) {
			cacheKeys = append(cacheKeys, "sys-config:"+vo.ConfigKey)
		}
	}

	// Clear Redis cache for all modified config keys
	for _, key := range cacheKeys {
		db.Redis.Del(ctx, key)
	}
}

// EditByCategory updates config values for a given category.
func EditByCategory(c *gin.Context, param *ConfigCategoryEditParam, userID string) {
	if len(param.Configs) == 0 {
		return
	}

	ctx := context.Background()
	now := time.Now()

	// Extract keys from param.Configs
	keys := make([]string, 0, len(param.Configs))
	for _, vo := range param.Configs {
		if vo.ConfigKey != "" {
			keys = append(keys, vo.ConfigKey)
		}
	}

	// Query existing configs by category + keys
	entities, err := db.Client.SysConfig.Query().
		Where(
			sysconfig.CategoryEQ(param.Category),
			sysconfig.ConfigKeyIn(keys...),
		).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询配置失败: "+err.Error(), 500))
	}

	// Build entity map keyed by ConfigKey
	entityMap := make(map[string]*ent.SysConfig, len(entities))
	for _, e := range entities {
		if e.ConfigKey != nil {
			entityMap[*e.ConfigKey] = e
		}
	}

	// Collect keys for cache cleanup
	var cacheKeys []string

	for _, vo := range param.Configs {
		entity, ok := entityMap[vo.ConfigKey]
		if !ok {
			panic(exception.NewBusinessError("分类 ["+param.Category+"] 下不存在配置: "+vo.ConfigKey, 400))
		}

		builder := db.Client.SysConfig.UpdateOneID(entity.ID).
			SetUpdatedAt(now)

		if vo.ConfigValue != "" {
			builder.SetConfigValue(vo.ConfigValue)
		}
		if userID != "" {
			builder.SetUpdatedBy(userID)
		}

		_, err := builder.Save(ctx)
		if err != nil {
			panic(exception.NewBusinessError("编辑配置失败: "+err.Error(), 500))
		}

		if vo.ConfigKey != "" {
			cacheKeys = append(cacheKeys, "sys-config:"+vo.ConfigKey)
		}
	}

	// Clear Redis cache for all modified keys
	for _, key := range cacheKeys {
		db.Redis.Del(ctx, key)
	}
}

// entToVO converts an ent SysConfig entity to a ConfigVO.
func entToVO(entity *ent.SysConfig) *ConfigVO {
	vo := &ConfigVO{
		ID:       entity.ID,
		SortCode: entity.SortCode,
	}

	if entity.ConfigKey != nil {
		vo.ConfigKey = *entity.ConfigKey
	}
	if entity.ConfigValue != nil {
		vo.ConfigValue = *entity.ConfigValue
	}
	if entity.Category != nil {
		vo.Category = *entity.Category
	}
	if entity.Remark != nil {
		vo.Remark = entity.Remark
	}
	if entity.Extra != nil {
		vo.Extra = entity.Extra
	}

	return vo
}

// Ensure predicate import is used (referenced via conds)
var _ = json.Valid
