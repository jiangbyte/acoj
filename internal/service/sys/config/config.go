package config

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"strings"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

// configCachePrefix is the Redis key prefix for config value caching.
const configCachePrefix = "sys-config:"

func init() {
	auth.RegisterPermission("sys:config:page", "sys/config", "BACKEND", "配置查询")
	auth.RegisterPermission("sys:config:list", "sys/config", "BACKEND", "配置列表查询")
	auth.RegisterPermission("sys:config:create", "sys/config", "BACKEND", "配置新增")
	auth.RegisterPermission("sys:config:modify", "sys/config", "BACKEND", "配置修改")
	auth.RegisterPermission("sys:config:edit", "sys/config", "BACKEND", "配置编辑")
	auth.RegisterPermission("sys:config:remove", "sys/config", "BACKEND", "配置删除")
	auth.RegisterPermission("sys:config:detail", "sys/config", "BACKEND", "配置详情")
	auth.RegisterPermission("sys:config:export", "sys/config", "BACKEND", "配置导出")
	auth.RegisterPermission("sys:config:template", "sys/config", "BACKEND", "配置导入模板")
	auth.RegisterPermission("sys:config:import", "sys/config", "BACKEND", "配置导入")
}

// getCachedValue reads a config value from Redis cache.
func getCachedValue(ctx context.Context, key string) (string, bool) {
	v, err := g.Redis().Get(ctx, configCachePrefix+key)
	if err == nil && !v.IsNil() {
		return v.String(), true
	}
	return "", false
}

// setCachedValue writes a config value to Redis cache (persistent, no expiry).
func setCachedValue(ctx context.Context, key, value string) {
	g.Redis().Set(ctx, configCachePrefix+key, value)
}

// delCachedValue removes a config value from Redis cache.
func delCachedValue(ctx context.Context, key string) {
	g.Redis().Del(ctx, configCachePrefix+key)
}

// GetConfigValue reads a config value by key, with Redis caching.
// This is the cross-module equivalent of Python's ConfigService.get_value_by_key().
func GetConfigValue(ctx context.Context, key string) (string, error) {
	// Try cache
	if val, ok := getCachedValue(ctx, key); ok {
		return val, nil
	}
	// Fall back to DB
	row, err := dao.SysConfig.Ctx().Ctx(ctx).
		Where("config_key", key).
		Fields("config_value").
		One()
	if err != nil {
		return "", err
	}
	if row == nil {
		return "", nil
	}
	val := row["config_value"].String()
	// Cache for next time
	setCachedValue(ctx, key, val)
	return val, nil
}

func Page(ctx context.Context, keyword, category string, current, size int) (*utility.PageRes, error) {
	m := dao.SysConfig.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("config_key LIKE ? OR remark LIKE ?", kw, kw)
	}
	if category != "" {
		m = m.Where("category", category)
	}
	count, err := m.Count()
	if err != nil {
		return nil, err
	}
	all, err := m.Page(current, size).All()
	if err != nil {
		return nil, err
	}
	list := all.List()
	for _, item := range list {
		if v, ok := item["extra"]; ok {
			item["ext_json"] = v
			delete(item, "extra")
		}
	}
	batchEnrichNames(ctx, list)
	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, configKey, configValue, category, remark string, sortCode int, extra string) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysConfig.Ctx().Ctx(ctx).Insert(g.Map{
		"id":           utility.GenerateID(),
		"config_key":   configKey,
		"config_value": configValue,
		"category":     category,
		"remark":       remark,
		"sort_code":    sortCode,
		"extra":        extra,
		"created_by":   loginId,
	})
	return err
}

func Modify(ctx context.Context, id, configKey, configValue, category, remark string, sortCode int, extra string) error {
	// Fetch original entity to know the config_key for cache invalidation
	original, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil {
		return err
	}

	updateData := g.Map{}
	if configKey != "" {
		updateData["config_key"] = configKey
	}
	if configValue != "" {
		updateData["config_value"] = configValue
	}
	if category != "" {
		updateData["category"] = category
	}
	if remark != "" {
		updateData["remark"] = remark
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if extra != "" {
		updateData["extra"] = extra
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		if err != nil {
			return err
		}
	}

	// Invalidate cache for the original key (and new key if it changed)
	if original != nil {
		delCachedValue(ctx, original["config_key"].String())
	}
	if configKey != "" && original != nil && configKey != original["config_key"].String() {
		delCachedValue(ctx, configKey)
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	// Fetch entities to get config_keys for cache invalidation
	entities, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(ids).All()
	if err != nil {
		return err
	}
	for _, entity := range entities {
		delCachedValue(ctx, entity["config_key"].String())
	}
	_, err = dao.SysConfig.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysConfig.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":           row["id"].String(),
		"config_key":   row["config_key"].String(),
		"config_value": row["config_value"].String(),
		"category":     row["category"].String(),
		"remark":       row["remark"].String(),
		"sort_code":    row["sort_code"].Int(),
		"ext_json":     row["extra"].String(),
		"created_at":   row["created_at"].String(),
		"created_by":   row["created_by"].String(),
		"updated_at":   row["updated_at"].String(),
		"updated_by":   row["updated_by"].String(),
	}
	enrichCreatorUpdater(ctx, result)
	return result, nil
}

func ListByCategory(ctx context.Context, category string) ([]g.Map, error) {
	m := dao.SysConfig.Ctx().Ctx(ctx)
	if category != "" {
		m = m.Where("category", category)
	}
	var list []g.Map
	if err := m.OrderAsc("sort_code").Scan(&list); err != nil {
		return nil, err
	}
	for _, item := range list {
		if v, ok := item["extra"]; ok {
			item["ext_json"] = v
			delete(item, "extra")
		}
	}
	return list, nil
}

func BatchEdit(ctx context.Context, items []g.Map) error {
	loginId := getLoginId(ctx)
	err := g.DB().Transaction(ctx, func(ctx context.Context, tx gdb.TX) error {
		for _, item := range items {
			id, _ := item["id"].(string)
			if id == "" {
				continue
			}
			// Check existence and get original config_key for cache invalidation
			original, err := tx.Model("sys_config").Ctx(ctx).WherePri(id).One()
			if err != nil {
				return err
			}
			if original == nil {
				return fmt.Errorf("配置不存在: %s", id)
			}

			updateData := g.Map{"updated_by": loginId}
			if v, ok := item["config_key"]; ok && v.(string) != "" {
				updateData["config_key"] = v
			}
			if v, ok := item["config_value"]; ok && v.(string) != "" {
				updateData["config_value"] = v
			}
			if v, ok := item["remark"]; ok && v.(string) != "" {
				updateData["remark"] = v
			}
			if v, ok := item["sort_code"]; ok && v.(int) != 0 {
				updateData["sort_code"] = v
			}
			if v, ok := item["ext_json"]; ok && v.(string) != "" {
				updateData["extra"] = v
			}
			_, err = tx.Model("sys_config").Ctx(ctx).WherePri(id).Update(updateData)
			if err != nil {
				return err
			}

			// Invalidate cache for the original config key
			delCachedValue(ctx, original["config_key"].String())
		}
		return nil
	})
	return err
}

func CategoryEdit(ctx context.Context, category string, configs []g.Map) error {
	loginId := getLoginId(ctx)
	err := g.DB().Transaction(ctx, func(ctx context.Context, tx gdb.TX) error {
		for _, item := range configs {
			key, _ := item["config_key"].(string)
			value, _ := item["config_value"].(string)
			if key == "" {
				continue
			}
			// Check existence
			row, err := tx.Model("sys_config").Ctx(ctx).
				Where("category", category).
				Where("config_key", key).
				One()
			if err != nil {
				return err
			}
			if row == nil {
				return fmt.Errorf("配置不存在: category=%s, key=%s", category, key)
			}

			_, err = tx.Model("sys_config").Ctx(ctx).
				Where("category", category).
				Where("config_key", key).
				Update(g.Map{
					"config_value": value,
					"updated_by":   loginId,
				})
			if err != nil {
				return err
			}

			// Invalidate cache for this config key
			delCachedValue(ctx, key)
		}
		return nil
	})
	return err
}

func Export(ctx context.Context, exportType string, selectedIds []string, current, size int) (*bytes.Buffer, error) {
	var records []g.Map
	switch exportType {
	case "current":
		pageSize := size
		if pageSize <= 0 {
			pageSize = 10
		}
		pageCurrent := current
		if pageCurrent <= 0 {
			pageCurrent = 1
		}
		m := dao.SysConfig.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysConfig.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.SysConfig.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}
	return utility.CreateExcelFromData(data, "配置数据")
}

func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"config_key", "config_value", "category", "remark", "sort_code", "ext_json"}
	return utility.CreateExcelTemplate(headers, "配置数据")
}

func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

	if file.Size > 5*1024*1024 {
		return nil, fmt.Errorf("文件大小不能超过5MB")
	}
	if !strings.HasSuffix(strings.ToLower(file.Filename), ".xlsx") {
		return nil, fmt.Errorf("仅支持.xlsx格式文件")
	}

	content, err := io.ReadAll(f)
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}

	rows, err := utility.ParseExcelFromBytes(content, true)
	if err != nil {
		return nil, err
	}

	if len(rows) == 0 {
		return nil, fmt.Errorf("导入数据不能为空")
	}

	imported := 0
	for _, row := range rows {
		id := utility.GenerateID()
		_, err := dao.SysConfig.Ctx().Ctx(ctx).Insert(g.Map{
			"id":           id,
			"config_key":   row["config_key"],
			"config_value": row["config_value"],
			"category":     row["category"],
			"remark":       row["remark"],
			"sort_code":    row["sort_code"],
			"extra":        row["ext_json"],
			"created_by":   getLoginId(ctx),
		})
		if err == nil {
			imported++
		}
	}

	return g.Map{
		"total":   imported,
		"message": fmt.Sprintf("成功导入%d条数据", imported),
	}, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	return ""
}

func ifEmpty(s, def string) string {
	if s == "" {
		return def
	}
	return s
}

func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	delete(result, "id")
	return result
}

func batchEnrichNames(ctx context.Context, list []g.Map) {
	for _, item := range list {
		enrichCreatorUpdater(ctx, item)
	}
}

func enrichCreatorUpdater(ctx context.Context, item g.Map) {
	if id, ok := item["created_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["created_name"] = row["nickname"].String()
		}
	}
	if id, ok := item["updated_by"].(string); ok && id != "" {
		row, _ := dao.SysUser.Ctx().Ctx(ctx).WherePri(id).Fields("nickname").One()
		if row != nil {
			item["updated_name"] = row["nickname"].String()
		}
	}
}
