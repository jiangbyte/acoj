package log

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/model/entity"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

func init() {
	auth.RegisterPermission("sys:log:page", "sys/log", "BACKEND", "日志查询")
	auth.RegisterPermission("sys:log:create", "sys/log", "BACKEND", "日志新增")
	auth.RegisterPermission("sys:log:modify", "sys/log", "BACKEND", "日志修改")
	auth.RegisterPermission("sys:log:remove", "sys/log", "BACKEND", "日志删除")
	auth.RegisterPermission("sys:log:detail", "sys/log", "BACKEND", "日志详情")
	auth.RegisterPermission("sys:log:export", "sys/log", "BACKEND", "日志导出")
	auth.RegisterPermission("sys:log:template", "sys/log", "BACKEND", "日志导入模板下载")
	auth.RegisterPermission("sys:log:import", "sys/log", "BACKEND", "日志导入")
}

// Page queries logs with pagination.
func Page(ctx context.Context, keyword, category, exeStatus string, current, size int) (*utility.PageRes, error) {
	m := dao.SysLog.Ctx().Ctx(ctx)
	if keyword != "" {
		kw := "%" + keyword + "%"
		m = m.Where("name LIKE ?", kw)
	}
	if category != "" {
		m = m.Where("category", category)
	}
	if exeStatus != "" {
		m = m.Where("exe_status", exeStatus)
	}
	m = m.OrderDesc("created_at")

	count, err := m.Count()
	if err != nil {
		return nil, err
	}

	var logs []*entity.SysLog
	if err := m.Page(current, size).Scan(&logs); err != nil {
		return nil, err
	}

	voList := make([]g.Map, 0, len(logs))
	for _, l := range logs {
		voList = append(voList, g.Map{
			"id":          l.Id,
			"category":    l.Category,
			"name":        l.Name,
			"exe_status":  l.ExeStatus,
			"exe_message": l.ExeMessage,
			"op_ip":       l.OpIp,
			"op_address":  l.OpAddress,
			"op_browser":  l.OpBrowser,
			"op_os":       l.OpOs,
			"class_name":  l.ClassName,
			"method_name": l.MethodName,
			"req_method":  l.ReqMethod,
			"req_url":     l.ReqUrl,
			"param_json":  l.ParamJson,
			"result_json": l.ResultJson,
			"op_time":     l.OpTime,
			"trace_id":    l.TraceId,
			"op_user":     l.OpUser,
			"sign_data":   l.SignData,
			"created_at":  l.CreatedAt,
			"created_by":  l.CreatedBy,
			"updated_at":  l.UpdatedAt,
			"updated_by":  l.UpdatedBy,
		})
	}

	return utility.NewPageRes(voList, count, current, size), nil
}

// Create inserts a new log record.
func Create(ctx context.Context, category, name, exeStatus, exeMessage, opIp, opAddress, opBrowser, opOs, className, methodName, reqMethod, reqUrl, paramJson, resultJson, opTime, traceId, opUser, signData string) error {
	loginId := getLoginId(ctx)
	_, err := dao.SysLog.Ctx().Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"category":    category,
		"name":        name,
		"exe_status":  exeStatus,
		"exe_message": exeMessage,
		"op_ip":       opIp,
		"op_address":  opAddress,
		"op_browser":  opBrowser,
		"op_os":       opOs,
		"class_name":  className,
		"method_name": methodName,
		"req_method":  reqMethod,
		"req_url":     reqUrl,
		"param_json":  paramJson,
		"result_json": resultJson,
		"op_time":     opTime,
		"trace_id":    traceId,
		"op_user":     opUser,
		"sign_data":   signData,
		"created_by":  loginId,
	})
	return err
}

// Modify updates an existing log record.
func Modify(ctx context.Context, id, category, name, exeStatus, exeMessage, opIp, opAddress, opBrowser, opOs, className, methodName, reqMethod, reqUrl, paramJson, resultJson, opTime, traceId, opUser, signData string) error {
	updateData := g.Map{}
	if category != "" {
		updateData["category"] = category
	}
	if name != "" {
		updateData["name"] = name
	}
	if exeStatus != "" {
		updateData["exe_status"] = exeStatus
	}
	if exeMessage != "" {
		updateData["exe_message"] = exeMessage
	}
	if opIp != "" {
		updateData["op_ip"] = opIp
	}
	if opAddress != "" {
		updateData["op_address"] = opAddress
	}
	if opBrowser != "" {
		updateData["op_browser"] = opBrowser
	}
	if opOs != "" {
		updateData["op_os"] = opOs
	}
	if className != "" {
		updateData["class_name"] = className
	}
	if methodName != "" {
		updateData["method_name"] = methodName
	}
	if reqMethod != "" {
		updateData["req_method"] = reqMethod
	}
	if reqUrl != "" {
		updateData["req_url"] = reqUrl
	}
	if paramJson != "" {
		updateData["param_json"] = paramJson
	}
	if resultJson != "" {
		updateData["result_json"] = resultJson
	}
	if opTime != "" {
		updateData["op_time"] = opTime
	}
	if traceId != "" {
		updateData["trace_id"] = traceId
	}
	if opUser != "" {
		updateData["op_user"] = opUser
	}
	if signData != "" {
		updateData["sign_data"] = signData
	}

	if len(updateData) > 0 {
		loginId := getLoginId(ctx)
		updateData["updated_by"] = loginId
		_, err := dao.SysLog.Ctx().Ctx(ctx).WherePri(id).Update(updateData)
		return err
	}
	return nil
}

// Remove deletes log records by IDs.
func Remove(ctx context.Context, ids []string) error {
	_, err := dao.SysLog.Ctx().Ctx(ctx).WherePri(ids).Delete()
	return err
}

// Detail returns a log record by ID.
func Detail(ctx context.Context, id string) (g.Map, error) {
	row, err := dao.SysLog.Ctx().Ctx(ctx).WherePri(id).One()
	if err != nil || row == nil {
		return nil, err
	}
	return g.Map{
		"id":          row["id"].String(),
		"category":    row["category"].String(),
		"name":        row["name"].String(),
		"exe_status":  row["exe_status"].String(),
		"exe_message": row["exe_message"].String(),
		"op_ip":       row["op_ip"].String(),
		"op_address":  row["op_address"].String(),
		"op_browser":  row["op_browser"].String(),
		"op_os":       row["op_os"].String(),
		"class_name":  row["class_name"].String(),
		"method_name": row["method_name"].String(),
		"req_method":  row["req_method"].String(),
		"req_url":     row["req_url"].String(),
		"param_json":  row["param_json"].String(),
		"result_json": row["result_json"].String(),
		"op_time":     row["op_time"].String(),
		"trace_id":    row["trace_id"].String(),
		"op_user":     row["op_user"].String(),
		"sign_data":   row["sign_data"].String(),
		"created_at":  row["created_at"].String(),
		"created_by":  row["created_by"].String(),
		"updated_at":  row["updated_at"].String(),
		"updated_by":  row["updated_by"].String(),
	}, nil
}

// DeleteByCategory deletes log records by category.
func DeleteByCategory(ctx context.Context, category string) error {
	_, err := dao.SysLog.Ctx().Ctx(ctx).Where("category", category).Delete()
	return err
}

// VisLineChartData returns visit line chart data for the last 7 days.
func VisLineChartData(ctx context.Context) (g.Map, error) {
	sevenDaysAgo := time.Now().AddDate(0, 0, -6).Format("2006-01-02 15:04:05")
	rows, err := g.DB().Model(dao.SysLog.Table).Ctx(ctx).
		Fields("DATE(op_time) as day, category, count(*) as count").
		Where("category LIKE ?", "VIS_%").
		WhereGTE("op_time", sevenDaysAgo).
		Group("day, category").
		OrderAsc("day").
		All()
	if err != nil {
		return nil, err
	}

	return buildDayCategoryChart(rows), nil
}

// VisPieChartData returns visit pie chart data grouped by category.
func VisPieChartData(ctx context.Context) (g.Map, error) {
	rows, err := g.DB().Model(dao.SysLog.Table).Ctx(ctx).
		Fields("category, count(*) as total").
		Where("category LIKE ?", "VIS_%").
		Group("category").
		All()
	if err != nil {
		return nil, err
	}

	var list []g.Map
	for _, r := range rows {
		list = append(list, g.Map{
			"category": r["category"].String(),
			"total":    r["total"].Int(),
		})
	}
	if list == nil {
		list = make([]g.Map, 0)
	}
	return g.Map{"list": list}, nil
}

// OpBarChartData returns operation bar chart data for the last 7 days.
func OpBarChartData(ctx context.Context) (g.Map, error) {
	sevenDaysAgo := time.Now().AddDate(0, 0, -6).Format("2006-01-02 15:04:05")
	rows, err := g.DB().Model(dao.SysLog.Table).Ctx(ctx).
		Fields("DATE(op_time) as day, category, count(*) as count").
		Where("category LIKE ?", "OP_%").
		WhereGTE("op_time", sevenDaysAgo).
		Group("day, category").
		OrderAsc("day").
		All()
	if err != nil {
		return nil, err
	}

	return buildDayCategoryChart(rows), nil
}

// OpPieChartData returns operation pie chart data grouped by category.
func OpPieChartData(ctx context.Context) (g.Map, error) {
	rows, err := g.DB().Model(dao.SysLog.Table).Ctx(ctx).
		Fields("category, count(*) as total").
		Where("category LIKE ?", "OP_%").
		Group("category").
		All()
	if err != nil {
		return nil, err
	}

	var list []g.Map
	for _, r := range rows {
		list = append(list, g.Map{
			"category": r["category"].String(),
			"total":    r["total"].Int(),
		})
	}
	if list == nil {
		list = make([]g.Map, 0)
	}
	return g.Map{"list": list}, nil
}

// --- Internal helpers ---

// buildDayCategoryChart builds a {days, series} chart structure from query rows.
// Each row should have "day", "category", "count" fields.
func buildDayCategoryChart(rows gdb.Result) g.Map {
	if rows == nil {
		rows = make(gdb.Result, 0)
	}

	daySet := make(map[string]bool)
	categoryMap := make(map[string]map[string]int) // category -> day -> count
	var categoryOrder []string

	for _, r := range rows {
		day := r["day"].String()
		category := r["category"].String()
		count := r["count"].Int()

		daySet[day] = true
		if _, ok := categoryMap[category]; !ok {
			categoryMap[category] = make(map[string]int)
			categoryOrder = append(categoryOrder, category)
		}
		categoryMap[category][day] = count
	}

	// Sort days
	var days []string
	for d := range daySet {
		days = append(days, d)
	}
	// Simple sort (days are date strings, sortable as strings)
	for i := 0; i < len(days); i++ {
		for j := i + 1; j < len(days); j++ {
			if days[i] > days[j] {
				days[i], days[j] = days[j], days[i]
			}
		}
	}
	if days == nil {
		days = make([]string, 0)
	}

	var series []g.Map
	for _, cat := range categoryOrder {
		data := make([]int, len(days))
		for i, d := range days {
			data[i] = categoryMap[cat][d]
		}
		series = append(series, g.Map{
			"name": cat,
			"data": data,
		})
	}
	if series == nil {
		series = make([]g.Map, 0)
	}

	return g.Map{
		"days":   days,
		"series": series,
	}
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

// Export exports log data as an Excel file.
func Export(ctx context.Context, exportType string, selectedIds []string, current, size int) (*bytes.Buffer, error) {
	var records []g.Map

	switch exportType {
	case "current":
		if size <= 0 {
			size = 10
		}
		if current <= 0 {
			current = 1
		}
		m := dao.SysLog.Ctx().Ctx(ctx)
		offset := (current - 1) * size
		if err := m.Limit(size).Offset(offset).Scan(&records); err != nil {
			return nil, err
		}
	case "selected":
		if len(selectedIds) == 0 {
			return nil, fmt.Errorf("请选择要导出的数据")
		}
		m := dao.SysLog.Ctx().Ctx(ctx).WherePri(selectedIds)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	default:
		m := dao.SysLog.Ctx().Ctx(ctx)
		if err := m.Scan(&records); err != nil {
			return nil, err
		}
	}

	data := make([]map[string]any, 0, len(records))
	for _, r := range records {
		item := cleanMapForExport(r)
		data = append(data, item)
	}

	return utility.CreateExcelFromData(data, "操作日志数据")
}

// DownloadTemplate downloads an import template Excel file for logs.
func DownloadTemplate(ctx context.Context) (*bytes.Buffer, error) {
	headers := []string{
		"category", "name", "exe_status", "exe_message",
		"op_ip", "op_address", "op_browser", "op_os",
		"class_name", "method_name", "req_method", "req_url",
		"param_json", "result_json", "op_time", "trace_id",
		"op_user", "sign_data",
	}
	return utility.CreateExcelTemplate(headers, "操作日志数据")
}

// Import imports log data from an uploaded Excel file.
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
		_, err := dao.SysLog.Ctx().Ctx(ctx).Insert(g.Map{
			"id":          id,
			"category":    row["category"],
			"name":        row["name"],
			"exe_status":  row["exe_status"],
			"exe_message": row["exe_message"],
			"op_ip":       row["op_ip"],
			"op_address":  row["op_address"],
			"op_browser":  row["op_browser"],
			"op_os":       row["op_os"],
			"class_name":  row["class_name"],
			"method_name": row["method_name"],
			"req_method":  row["req_method"],
			"req_url":     row["req_url"],
			"param_json":  row["param_json"],
			"result_json": row["result_json"],
			"op_time":     row["op_time"],
			"trace_id":    row["trace_id"],
			"op_user":     row["op_user"],
			"sign_data":   row["sign_data"],
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

// cleanMapForExport replaces nil values with empty strings and removes the id key.
func cleanMapForExport(m g.Map) map[string]any {
	result := make(map[string]any, len(m))
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
