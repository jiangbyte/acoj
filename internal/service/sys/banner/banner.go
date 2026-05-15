package banner

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
	auth.RegisterPermission("sys:banner:page", "sys/banner", "BACKEND", "轮播图查询")
	auth.RegisterPermission("sys:banner:create", "sys/banner", "BACKEND", "轮播图新增")
	auth.RegisterPermission("sys:banner:modify", "sys/banner", "BACKEND", "轮播图修改")
	auth.RegisterPermission("sys:banner:remove", "sys/banner", "BACKEND", "轮播图删除")
	auth.RegisterPermission("sys:banner:detail", "sys/banner", "BACKEND", "轮播图详情")
	auth.RegisterPermission("sys:banner:export", "sys/banner", "BACKEND", "轮播图导出")
	auth.RegisterPermission("sys:banner:template", "sys/banner", "BACKEND", "轮播图导入模板")
	auth.RegisterPermission("sys:banner:import", "sys/banner", "BACKEND", "轮播图导入")
}

func Page(ctx context.Context, current, size int) (*utility.PageRes, error) {
	m := dao.SysBanner.Ctx().Ctx(ctx).OrderAsc("sort_code")
	count, err := m.Count()
	if err != nil {
		return nil, err
	}
	all, err := m.Page(current, size).All()
	if err != nil {
		return nil, err
	}
	list := all.List()
	// Enrich created_name/updated_name
	batchEnrichNames(ctx, list)
	return utility.NewPageRes(list, count, current, size), nil
}

func Create(ctx context.Context, title, image, category, bannerType, position, url, linkType, summary, description string, sortCode int) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysBanner.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"title":       title,
		"image":       image,
		"category":    category,
		"type":        bannerType,
		"position":    position,
		"url":         url,
		"link_type":   linkType,
		"summary":     summary,
		"description": description,
		"sort_code":   sortCode,
		"created_by":  loginId,
	})
	return err
}

func Modify(ctx context.Context, id, title, image, category, bannerType, position, url, linkType, summary, description string, sortCode int) error {
	updateData := g.Map{}
	if title != "" {
		updateData["title"] = title
	}
	if image != "" {
		updateData["image"] = image
	}
	if category != "" {
		updateData["category"] = category
	}
	if bannerType != "" {
		updateData["type"] = bannerType
	}
	if position != "" {
		updateData["position"] = position
	}
	if url != "" {
		updateData["url"] = url
	}
	if linkType != "" {
		updateData["link_type"] = linkType
	}
	if summary != "" {
		updateData["summary"] = summary
	}
	if description != "" {
		updateData["description"] = description
	}
	if sortCode != 0 {
		updateData["sort_code"] = sortCode
	}
	if len(updateData) > 0 {
		updateData["updated_by"] = getLoginId(ctx)
		_, err := dao.SysBanner.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysBanner.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysBanner.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	result := g.Map{
		"id":          row["id"].String(),
		"title":       row["title"].String(),
		"image":       row["image"].String(),
		"category":    row["category"].String(),
		"type":        row["type"].String(),
		"position":    row["position"].String(),
		"url":         row["url"].String(),
		"link_type":   row["link_type"].String(),
		"summary":     row["summary"].String(),
		"description": row["description"].String(),
		"sort_code":   row["sort_code"].Int(),
		"view_count":  row["view_count"].Int(),
		"click_count": row["click_count"].Int(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
	}
	enrichCreatorUpdater(ctx, result)
	return result, nil
}

// Export exports banner data as an Excel file.
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
		m := dao.SysBanner.Ctx().Ctx(ctx)
		offset := (pageCurrent - 1) * pageSize
		if err := m.Limit(pageSize).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysBanner.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default: // "all"
		m := dao.SysBanner.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]interface{}, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "轮播图数据")
}

// DownloadTemplate downloads an import template Excel file.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{"title", "image", "category", "type", "position", "url", "link_type", "summary", "description", "sort_code"}
	return utility.CreateExcelTemplate(headers, "轮播图数据")
}

// Import imports banner data from an uploaded Excel file.
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
		_, err := dao.SysBanner.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          id,
			"title":       row["title"],
			"image":       row["image"],
			"category":    row["category"],
			"type":        row["type"],
			"position":    row["position"],
			"url":         row["url"],
			"link_type":   row["link_type"],
			"summary":     row["summary"],
			"description": row["description"],
			"sort_code":   row["sort_code"],
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
	delete(result, "password")
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
