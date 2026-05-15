package log

import (
	"context"
	"time"

	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
	"hei-goframe/internal/model/entity"
	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

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
	sevenDaysAgo := time.Now().AddDate(0, 0, -7).Format("2006-01-02 15:04:05")
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
	sevenDaysAgo := time.Now().AddDate(0, 0, -7).Format("2006-01-02 15:04:05")
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
