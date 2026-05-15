package config

import (
	"context"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/sysconfig"
)

type PageParam struct {
	Page     int    `form:"page" json:"page"`
	Size     int    `form:"size" json:"size"`
	Keyword  string `form:"keyword" json:"keyword"`
	Category string `form:"category" json:"category"`
	Status   string `form:"status" json:"status"`
}

type ConfigVO struct {
	ID          string `json:"id"`
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
	Category    string `json:"category"`
	Name        string `json:"name"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Type        string `json:"type"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type ConfigCreateReq struct {
	ConfigKey   string `json:"config_key" binding:"required"`
	ConfigValue string `json:"config_value" binding:"required"`
	Category    string `json:"category"`
	Name        string `json:"name"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Type        string `json:"type"`
}

type ConfigModifyReq struct {
	ID          string `json:"id" binding:"required"`
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
	Category    string `json:"category"`
	Name        string `json:"name"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	Type        string `json:"type"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

func toVO(c *ent.SysConfig) ConfigVO {
	vo := ConfigVO{
		ID:          c.ID,
		ConfigKey:   c.ConfigKey,
		ConfigValue: c.ConfigValue,
		Category:    c.Category,
		Name:        c.Name,
		Description: c.Description,
		SortCode:    c.SortCode,
		Status:      c.Status,
		Type:        c.Type,
		CreatedAt:   c.CreatedAt.Format("2006-01-02 15:04:05"),
		UpdatedAt:   c.UpdatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   c.CreatedBy,
		UpdatedBy:   c.UpdatedBy,
	}
	return vo
}

func Page(page, size int, keyword, category, status string) (int, []*ent.SysConfig, error) {
	ctx := context.Background()
	q := db.Client.SysConfig.Query()

	if keyword != "" {
		q = q.Where(
			sysconfig.Or(
				sysconfig.ConfigKeyContains(keyword),
				sysconfig.ConfigValueContains(keyword),
				sysconfig.NameContains(keyword),
			),
		)
	}
	if category != "" {
		q = q.Where(sysconfig.Category(category))
	}
	if status != "" {
		q = q.Where(sysconfig.Status(status))
	}

	total, err := q.Count(ctx)
	if err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}

	items, err := q.
		Order(ent.Desc(sysconfig.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *ConfigCreateReq, loginID string) (*ent.SysConfig, error) {
	ctx := context.Background()
	now := time.Now()
	q := db.Client.SysConfig.Create().
		SetID(utils.NextID()).
		SetConfigKey(req.ConfigKey).
		SetConfigValue(req.ConfigValue).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)
	if req.Category != "" {
		q.SetCategory(req.Category)
	}
	if req.Name != "" {
		q.SetName(req.Name)
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}
	if req.SortCode > 0 {
		q.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	}
	if req.Type != "" {
		q.SetType(req.Type)
	}
	return q.Save(ctx)
}

func Modify(req *ConfigModifyReq, loginID string) (*ent.SysConfig, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.SysConfig.UpdateOneID(req.ID)

	if req.ConfigKey != "" {
		u.SetConfigKey(req.ConfigKey)
	}
	if req.ConfigValue != "" {
		u.SetConfigValue(req.ConfigValue)
	}
	if req.Category != "" {
		u.SetCategory(req.Category)
	}
	if req.Name != "" {
		u.SetName(req.Name)
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}
	if req.SortCode > 0 {
		u.SetSortCode(req.SortCode)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.Type != "" {
		u.SetType(req.Type)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()
	_, err := db.Client.SysConfig.Delete().Where(sysconfig.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.SysConfig, error) {
	ctx := context.Background()
	return db.Client.SysConfig.Get(ctx, id)
}

func QueryByCategory(category string) ([]*ent.SysConfig, error) {
	ctx := context.Background()
	return db.Client.SysConfig.Query().
		Where(sysconfig.Category(category)).
		Order(ent.Desc(sysconfig.FieldCreatedAt)).
		All(ctx)
}

type EditBatchItem struct {
	ID          string `json:"id"`
	ConfigValue string `json:"config_value"`
}

type EditBatchReq struct {
	Configs []EditBatchItem `json:"configs"`
}

type EditByCategoryItem struct {
	ConfigKey   string `json:"config_key"`
	ConfigValue string `json:"config_value"`
}

type EditByCategoryReq struct {
	Category string               `json:"category"`
	Configs  []EditByCategoryItem `json:"configs"`
}

func EditBatch(configs []EditBatchItem) error {
	ctx := context.Background()
	for _, item := range configs {
		err := db.Client.SysConfig.UpdateOneID(item.ID).
			SetConfigValue(item.ConfigValue).
			Exec(ctx)
		if err != nil {
			return err
		}
	}
	return nil
}

func EditByCategory(category string, configs []EditByCategoryItem) error {
	ctx := context.Background()
	for _, item := range configs {
		_, err := db.Client.SysConfig.Update().
			Where(sysconfig.Category(category), sysconfig.ConfigKey(item.ConfigKey)).
			SetConfigValue(item.ConfigValue).
			Save(ctx)
		if err != nil {
			return err
		}
	}
	return nil
}
