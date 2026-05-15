package position

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/consts"
	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:position:page", "sys/position", "BACKEND", "职位查询")
	auth.RegisterPermission("sys:position:create", "sys/position", "BACKEND", "职位新增")
	auth.RegisterPermission("sys:position:modify", "sys/position", "BACKEND", "职位修改")
	auth.RegisterPermission("sys:position:remove", "sys/position", "BACKEND", "职位删除")
	auth.RegisterPermission("sys:position:detail", "sys/position", "BACKEND", "职位详情")
	auth.RegisterPermission("sys:position:export", "sys/position", "BACKEND", "职位导出")
	auth.RegisterPermission("sys:position:template", "sys/position", "BACKEND", "职位导入模板")
	auth.RegisterPermission("sys:position:import", "sys/position", "BACKEND", "职位导入")
}

func Page(ctx context.Context, keyword, status, groupId, orgId string, current, size int) (*utility.PageRes, error) {
	if groupId == "" {
		return utility.NewPageRes([]g.Map{}, 0, current, size), nil
	}

	m := dao.SysPosition.Ctx().Ctx(ctx).OrderAsc("sort_code")
	if groupId != "" {
		m = m.Where("group_id", groupId)
	}
	if orgId != "" {
		m = m.Where("org_id", orgId)
	}
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("name LIKE ? OR code LIKE ?", kw, kw)
	}
	if status != "" {
		m = m.Where("status", status)
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
	batchEnrichNames(ctx, list)
	batchEnrichNamePaths(ctx, list)
	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, code, name, category, orgId, groupId, description, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysPosition.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"code":        code,
		"name":        name,
		"category":    category,
		"org_id":      orgId,
		"group_id":    groupId,
		"description": description,
		"status":      ifEmpty(status, consts.StatusEnabled),
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, code, name, category, orgId, groupId, description, status string, sortCode int) error {
	entity := findById(ctx, id)
	if entity == nil {
		return errors.New("数据不存在")
	}

	updateData := g.Map{}
	if code != "" {
		updateData["code"] = code
	}
	if name != "" {
		updateData["name"] = name
	}
	if category != "" {
		updateData["category"] = category
	}
	if orgId != "" {
		updateData["org_id"] = orgId
	}
	if groupId != "" {
		updateData["group_id"] = groupId
	}
	if description != "" {
		updateData["description"] = description
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}

	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	count, err := dao.SysUser.Ctx().Ctx(ctx).Where("position_id in (?)", ids).Count()
	if err != nil {
		return err
	}
	if count > 0 {
		return errors.New("职位存在关联用户，无法删除")
	}

	_, err = dao.SysPosition.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":          row["id"].String(),
		"code":        row["code"].String(),
		"name":        row["name"].String(),
		"category":    row["category"].String(),
		"org_id":      row["org_id"].String(),
		"group_id":    row["group_id"].String(),
		"description": row["description"].String(),
		"status":      row["status"].String(),
		"sort_code":   row["sort_code"].Int(),
		"extra":       row["extra"].String(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
	}
	enrichCreatorUpdater(ctx, result)
	enrichNamePaths(ctx, result)
	return result, nil
}

// Export exports position data as an Excel file.
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
		m := dao.SysPosition.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysPosition.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default: // "all"
		m := dao.SysPosition.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "职位数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"code", "name", "category", "org_id", "group_id", "description", "status", "sort_code", "extra"}
	return utility.CreateExcelTemplate(headers, "职位数据")
}

// Import imports position data from an uploaded Excel file.
func Import(ctx context.Context, file ghttp.UploadFile) (g.Map, error) {
	f, err := file.Open()
	if err != nil {
		return nil, fmt.Errorf("无法读取上传文件: %w", err)
	}
	defer f.Close()

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
		_, err := dao.SysPosition.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          id,
			"code":        row["code"],
			"name":        row["name"],
			"category":    row["category"],
			"org_id":      row["org_id"],
			"group_id":    row["group_id"],
			"description": row["description"],
			"status":      row["status"],
			"sort_code":   row["sort_code"],
			"extra":       row["extra"],
			"created_by":  getLoginId(ctx),
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

func findById(ctx context.Context, id string) g.Map {
	row, err := dao.SysPosition.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil
	}
	return row.Map()
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

// cleanMapForExport removes nil values and converts types for Excel export.
func cleanMapForExport(m g.Map) map[string]interface{} {
	result := make(map[string]interface{}, len(m))
	for k, v := range m {
		if v == nil {
			result[k] = ""
		} else {
			result[k] = v
		}
	}
	// Remove sensitive/internal fields
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

func resolveNamePath(ctx context.Context, table string, id string) []string {
	if id == "" {
		return nil
	}
	var names []string
	currentId := id
	for currentId != "" {
		sql := "SELECT id, name, parent_id FROM " + table + " WHERE id = ?"
		row, err := g.DB().Ctx(ctx).GetOne(ctx, sql, currentId)
		if err != nil || row == nil {
			break
		}
		names = append([]string{row["name"].String()}, names...)
		currentId = row["parent_id"].String()
	}
	return names
}

func enrichNamePaths(ctx context.Context, item g.Map) {
	if orgId, ok := item["org_id"].(string); ok && orgId != "" {
		item["org_names"] = resolveNamePath(ctx, "sys_org", orgId)
	}
	if groupId, ok := item["group_id"].(string); ok && groupId != "" {
		item["group_names"] = resolveNamePath(ctx, "sys_group", groupId)
	}
}

func batchEnrichNamePaths(ctx context.Context, list []g.Map) {
	for _, item := range list {
		enrichNamePaths(ctx, item)
	}
}
