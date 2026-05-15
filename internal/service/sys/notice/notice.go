package notice

import (
	"bytes"
	"context"
	"fmt"
	"io"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:notice:page", "sys/notice", "BACKEND", "通知查询")
	auth.RegisterPermission("sys:notice:create", "sys/notice", "BACKEND", "通知新增")
	auth.RegisterPermission("sys:notice:modify", "sys/notice", "BACKEND", "通知修改")
	auth.RegisterPermission("sys:notice:remove", "sys/notice", "BACKEND", "通知删除")
	auth.RegisterPermission("sys:notice:detail", "sys/notice", "BACKEND", "通知详情")
	auth.RegisterPermission("sys:notice:export", "sys/notice", "BACKEND", "通知导出")
	auth.RegisterPermission("sys:notice:template", "sys/notice", "BACKEND", "通知导入模板")
	auth.RegisterPermission("sys:notice:import", "sys/notice", "BACKEND", "通知导入")
}

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	m := dao.SysNotice.Ctx().Ctx(ctx).OrderAsc("sort_code")
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
	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, title, category, noticeType, summary, content, cover, level, position, status string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysNotice.Ctx().Ctx(ctx).Insert(g.Map{
		"id":         utility.GenerateID(),
		"title":      title,
		"category":   category,
		"type":       noticeType,
		"summary":    summary,
		"content":    content,
		"cover":      cover,
		"level":      level,
		"position":   position,
		"status":     ifEmpty(status, "ENABLED"),
		"sort_code":  sortCode,
		"created_by": loginId,
	})
	return err
}

func Modify(ctx context.Context, id, title, category, noticeType, summary, content, cover, level, position, status string, sortCode int) error {
	updateData := g.Map{}
	if title != "" {
		updateData["title"] = title
	}
	if category != "" {
		updateData["category"] = category
	}
	if noticeType != "" {
		updateData["type"] = noticeType
	}
	if summary != "" {
		updateData["summary"] = summary
	}
	if content != "" {
		updateData["content"] = content
	}
	if cover != "" {
		updateData["cover"] = cover
	}
	if level != "" {
		updateData["level"] = level
	}
	if position != "" {
		updateData["position"] = position
	}
	if status != "" {
		updateData["status"] = status
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysNotice.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysNotice.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysNotice.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":         row["id"].String(),
		"title":      row["title"].String(),
		"category":   row["category"].String(),
		"type":       row["type"].String(),
		"summary":    row["summary"].String(),
		"content":    row["content"].String(),
		"cover":      row["cover"].String(),
		"level":      row["level"].String(),
		"view_count": row["view_count"].Int(),
		"is_top":     row["is_top"].String(),
		"position":   row["position"].String(),
		"status":     row["status"].String(),
		"sort_code":  row["sort_code"].Int(),
		"created_at": row["created_at"].String(),
		"created_by": row["created_by"].String(),
		"updated_at": row["updated_at"].String(),
		"updated_by": row["updated_by"].String(),
	}
	enrichCreatorUpdater(ctx, result)
	return result, nil
}

// Export exports notice data as an Excel file.
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
		m := dao.SysNotice.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysNotice.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default: // "all"
		m := dao.SysNotice.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "通知数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"title", "category", "type", "summary", "content", "cover", "level", "view_count", "is_top", "position", "status", "sort_code"}
	return utility.CreateExcelTemplate(headers, "通知数据")
}

// Import imports notice data from an uploaded Excel file.
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
		_, err := dao.SysNotice.Ctx().Ctx(ctx).Insert(g.Map{
			"id":         id,
			"title":      row["title"],
			"category":   row["category"],
			"type":       row["type"],
			"summary":    row["summary"],
			"content":    row["content"],
			"cover":      row["cover"],
			"level":      row["level"],
			"position":   row["position"],
			"status":     row["status"],
			"sort_code":  row["sort_code"],
			"created_by": getLoginId(ctx),
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
