package log

import (
	"context"
	"strings"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/syslog"
)

type PageParam struct {
	Page      int    `form:"page" json:"page"`
	Size      int    `form:"size" json:"size"`
	Keyword   string `form:"keyword" json:"keyword"`
	ExeStatus string `form:"exe_status" json:"exe_status"`
	ReqMethod string `form:"req_method" json:"req_method"`
}

type SysLogVO struct {
	ID          string `json:"id"`
	Category    string `json:"category"`
	Name        string `json:"name"`
	ExeStatus   string `json:"exe_status"`
	ExeMessage  string `json:"exe_message"`
	TraceID     string `json:"trace_id"`
	OpIP        string `json:"op_ip"`
	OpAddress   string `json:"op_address"`
	OpBrowser   string `json:"op_browser"`
	OpOs        string `json:"op_os"`
	ClassName   string `json:"class_name"`
	MethodName  string `json:"method_name"`
	ReqMethod   string `json:"req_method"`
	ReqURL      string `json:"req_url"`
	ParamJSON   string `json:"param_json"`
	ResultJSON  string `json:"result_json"`
	OpTime      int    `json:"op_time"`
	OpUser      string `json:"op_user"`
	SignData    string `json:"sign_data"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	CreatedName string `json:"created_name"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
	UpdatedName string `json:"updated_name"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

type CleanReq struct {
	Category string `json:"category" binding:"required"`
}

var LogExportFieldNames = map[string]string{
	"name":       "操作名称",
	"category":   "日志类别",
	"exe_status": "执行状态",
	"op_ip":      "操作IP",
	"req_method": "请求方法",
	"req_url":    "请求URL",
	"op_time":    "操作耗时",
	"op_user":    "操作用户",
	"created_at": "创建时间",
}

var LogExportFields = []string{"name", "category", "exe_status", "op_ip", "req_method", "req_url", "op_time", "op_user", "created_at"}

func toVO(s *ent.SysLog) SysLogVO {
	return SysLogVO{
		ID:         s.ID,
		Category:   s.Category,
		Name:       s.Name,
		ExeStatus:  s.ExeStatus,
		ExeMessage: s.ExeMessage,
		TraceID:    s.TraceID,
		OpIP:       s.OpIP,
		OpAddress:  s.OpAddress,
		OpBrowser:  s.OpBrowser,
		OpOs:       s.OpOs,
		ClassName:  s.ClassName,
		MethodName: s.MethodName,
		ReqMethod:  s.ReqMethod,
		ReqURL:     s.ReqURL,
		ParamJSON:  s.ParamJSON,
		ResultJSON: s.ResultJSON,
		OpTime:     s.OpTime,
		OpUser:     s.OpUser,
		SignData:   s.SignData,
		CreatedAt:  s.CreatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:  s.CreatedBy,
	}
}

func Page(page, size int, keyword, exeStatus, reqMethod string) (int, []*ent.SysLog, error) {
	ctx := context.Background()
	q := db.Client.SysLog.Query()

	if keyword != "" {
		q = q.Where(
			syslog.Or(
				syslog.NameContains(keyword),
				syslog.CategoryContains(keyword),
			),
		)
	}
	if exeStatus != "" {
		q = q.Where(syslog.ExeStatus(exeStatus))
	}
	if reqMethod != "" {
		q = q.Where(syslog.ReqMethod(reqMethod))
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

	// Performance: exclude LONGTEXT columns (param_json, result_json, exe_message, sign_data) from list query
	items, err := q.
		Select(
			syslog.FieldID,
			syslog.FieldCategory,
			syslog.FieldName,
			syslog.FieldExeStatus,
			syslog.FieldTraceID,
			syslog.FieldOpIP,
			syslog.FieldOpAddress,
			syslog.FieldOpBrowser,
			syslog.FieldOpOs,
			syslog.FieldClassName,
			syslog.FieldMethodName,
			syslog.FieldReqMethod,
			syslog.FieldReqURL,
			syslog.FieldOpTime,
			syslog.FieldOpUser,
			syslog.FieldCreatedAt,
			syslog.FieldCreatedBy,
		).
		Order(ent.Desc(syslog.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Detail(id string) (*ent.SysLog, error) {
	ctx := context.Background()
	return db.Client.SysLog.Get(ctx, id)
}

func Remove(ids []string) error {
	ctx := context.Background()
	_, err := db.Client.SysLog.Delete().Where(syslog.IDIn(ids...)).Exec(ctx)
	return err
}

func DeleteByCategory(category string) error {
	ctx := context.Background()
	_, err := db.RawDB.ExecContext(ctx, "DELETE FROM sys_log WHERE category = ?", category)
	return err
}

func QueryAll() ([]*ent.SysLog, error) {
	ctx := context.Background()
	return db.Client.SysLog.Query().Order(ent.Desc(syslog.FieldCreatedAt)).All(ctx)
}

func Create(vo SysLogVO) (*ent.SysLog, error) {
	ctx := context.Background()
	now := time.Now()
	return db.Client.SysLog.Create().
		SetID(utils.NextID()).
		SetCategory(vo.Category).
		SetName(vo.Name).
		SetExeStatus(vo.ExeStatus).
		SetExeMessage(vo.ExeMessage).
		SetTraceID(vo.TraceID).
		SetOpIP(vo.OpIP).
		SetOpAddress(vo.OpAddress).
		SetOpBrowser(vo.OpBrowser).
		SetOpOs(vo.OpOs).
		SetClassName(vo.ClassName).
		SetMethodName(vo.MethodName).
		SetReqMethod(vo.ReqMethod).
		SetReqURL(vo.ReqURL).
		SetParamJSON(vo.ParamJSON).
		SetResultJSON(vo.ResultJSON).
		SetOpTime(vo.OpTime).
		SetOpUser(vo.OpUser).
		SetSignData(vo.SignData).
		SetCreatedAt(now).
		SetCreatedBy(vo.CreatedBy).
		Save(ctx)
}

func Modify(vo SysLogVO) error {
	ctx := context.Background()
	_, err := db.Client.SysLog.Update().
		Where(syslog.IDEQ(vo.ID)).
		SetCategory(vo.Category).
		SetName(vo.Name).
		SetExeStatus(vo.ExeStatus).
		SetExeMessage(vo.ExeMessage).
		SetTraceID(vo.TraceID).
		SetOpIP(vo.OpIP).
		SetOpAddress(vo.OpAddress).
		SetOpBrowser(vo.OpBrowser).
		SetOpOs(vo.OpOs).
		SetClassName(vo.ClassName).
		SetMethodName(vo.MethodName).
		SetReqMethod(vo.ReqMethod).
		SetReqURL(vo.ReqURL).
		SetParamJSON(vo.ParamJSON).
		SetResultJSON(vo.ResultJSON).
		SetOpTime(vo.OpTime).
		SetOpUser(vo.OpUser).
		SetSignData(vo.SignData).
		Save(ctx)
	return err
}

// resolveNicknames batch-resolves user IDs to nicknames from sys_user table.
func resolveNicknames(userIDs []string) map[string]string {
	result := map[string]string{}
	if len(userIDs) == 0 {
		return result
	}
	ctx := context.Background()
	placeholders := make([]string, len(userIDs))
	args := make([]interface{}, len(userIDs))
	for i, id := range userIDs {
		placeholders[i] = "?"
		args[i] = id
	}
	query := "SELECT id, nickname FROM sys_user WHERE id IN (" + strings.Join(placeholders, ",") + ")"
	rows, err := db.RawDB.QueryContext(ctx, query, args...)
	if err != nil {
		return result
	}
	defer rows.Close()
	for rows.Next() {
		var id, nickname string
		if err := rows.Scan(&id, &nickname); err == nil {
			result[id] = nickname
		}
	}
	return result
}

func CreateFromImport(row map[string]string) (*ent.SysLog, error) {
	ctx := context.Background()
	now := time.Now()

	return db.Client.SysLog.Create().
		SetID(utils.NextID()).
		SetName(row["操作名称"]).
		SetCategory(row["日志类别"]).
		SetExeStatus(row["执行状态"]).
		SetOpIP(row["操作IP"]).
		SetReqMethod(row["请求方法"]).
		SetReqURL(row["请求URL"]).
		SetCreatedAt(now).
		SetCreatedBy("import").
		Save(ctx)
}

// Log visualization types

type LogBarChartData struct {
	Days   []string            `json:"days"`
	Series []LogCategorySeries `json:"series"`
}

type LogCategorySeries struct {
	Name string `json:"name"`
	Data []int  `json:"data"`
}

type LogPieChartData struct {
	Data []LogCategoryTotal `json:"data"`
}

type LogCategoryTotal struct {
	Category string `json:"category"`
	Total    int    `json:"total"`
}

// VisLineChartData returns daily login/logout counts for the last 7 days.
func VisLineChartData() (*LogBarChartData, error) {
	ctx := context.Background()
	now := time.Now()

	days := make([]string, 7)
	dayIndex := make(map[string]int)
	for i := 6; i >= 0; i-- {
		day := now.AddDate(0, 0, -i).Format("2006-01-02")
		days[6-i] = day
		dayIndex[day] = 6 - i
	}

	startDate := now.AddDate(0, 0, -6).Format("2006-01-02") + " 00:00:00"

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT DATE(created_at) AS day, category, COUNT(*) AS cnt
		 FROM sys_log
		 WHERE category IN ('LOGIN','LOGOUT')
		   AND created_at >= ?
		 GROUP BY DATE(created_at), category
		 ORDER BY day`,
		startDate,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	catName := map[string]string{
		"LOGIN":  "登录",
		"LOGOUT": "登出",
	}

	catData := make(map[string][]int)
	catOrder := make([]string, 0)

	for rows.Next() {
		var day, category string
		var count int
		if err := rows.Scan(&day, &category, &count); err != nil {
			continue
		}
		name := catName[category]
		if name == "" {
			name = category
		}
		if _, ok := catData[name]; !ok {
			catData[name] = make([]int, 7)
			catOrder = append(catOrder, name)
		}
		if idx, ok := dayIndex[day]; ok {
			catData[name][idx] = count
		}
	}

	data := &LogBarChartData{
		Days:   days,
		Series: make([]LogCategorySeries, 0, len(catOrder)),
	}
	for _, name := range catOrder {
		data.Series = append(data.Series, LogCategorySeries{
			Name: name,
			Data: catData[name],
		})
	}
	if data.Series == nil {
		data.Series = []LogCategorySeries{}
	}

	return data, nil
}

// VisPieChartData returns total counts grouped by category for LOGIN/LOGOUT.
func VisPieChartData() (*LogPieChartData, error) {
	ctx := context.Background()

	catName := map[string]string{
		"LOGIN":  "登录",
		"LOGOUT": "登出",
	}

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT category, COUNT(*) AS cnt
		 FROM sys_log
		 WHERE category IN ('LOGIN','LOGOUT')
		 GROUP BY category`,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	data := &LogPieChartData{
		Data: make([]LogCategoryTotal, 0),
	}
	for rows.Next() {
		var category string
		var total int
		if err := rows.Scan(&category, &total); err != nil {
			continue
		}
		name := catName[category]
		if name == "" {
			name = category
		}
		data.Data = append(data.Data, LogCategoryTotal{
			Category: name,
			Total:    total,
		})
	}
	if data.Data == nil {
		data.Data = []LogCategoryTotal{}
	}

	return data, nil
}

// OpBarChartData returns daily operate/exception counts for the last 7 days.
func OpBarChartData() (*LogBarChartData, error) {
	ctx := context.Background()
	now := time.Now()

	days := make([]string, 7)
	dayIndex := make(map[string]int)
	for i := 6; i >= 0; i-- {
		day := now.AddDate(0, 0, -i).Format("2006-01-02")
		days[6-i] = day
		dayIndex[day] = 6 - i
	}

	startDate := now.AddDate(0, 0, -6).Format("2006-01-02") + " 00:00:00"

	catName := map[string]string{
		"OPERATE":   "操作",
		"EXCEPTION": "异常",
	}

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT DATE(created_at) AS day, category, COUNT(*) AS cnt
		 FROM sys_log
		 WHERE category IN ('OPERATE','EXCEPTION')
		   AND created_at >= ?
		 GROUP BY DATE(created_at), category
		 ORDER BY day`,
		startDate,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	catData := make(map[string][]int)
	catOrder := make([]string, 0)

	for rows.Next() {
		var day, category string
		var count int
		if err := rows.Scan(&day, &category, &count); err != nil {
			continue
		}
		name := catName[category]
		if name == "" {
			name = category
		}
		if _, ok := catData[name]; !ok {
			catData[name] = make([]int, 7)
			catOrder = append(catOrder, name)
		}
		if idx, ok := dayIndex[day]; ok {
			catData[name][idx] = count
		}
	}

	data := &LogBarChartData{
		Days:   days,
		Series: make([]LogCategorySeries, 0, len(catOrder)),
	}
	for _, name := range catOrder {
		data.Series = append(data.Series, LogCategorySeries{
			Name: name,
			Data: catData[name],
		})
	}
	if data.Series == nil {
		data.Series = []LogCategorySeries{}
	}

	return data, nil
}

// OpPieChartData returns total counts grouped by category for OPERATE/EXCEPTION.
func OpPieChartData() (*LogPieChartData, error) {
	ctx := context.Background()

	catName := map[string]string{
		"OPERATE":   "操作",
		"EXCEPTION": "异常",
	}

	rows, err := db.RawDB.QueryContext(ctx,
		`SELECT category, COUNT(*) AS cnt
		 FROM sys_log
		 WHERE category IN ('OPERATE','EXCEPTION')
		 GROUP BY category`,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	data := &LogPieChartData{
		Data: make([]LogCategoryTotal, 0),
	}
	for rows.Next() {
		var category string
		var total int
		if err := rows.Scan(&category, &total); err != nil {
			continue
		}
		name := catName[category]
		if name == "" {
			name = category
		}
		data.Data = append(data.Data, LogCategoryTotal{
			Category: name,
			Total:    total,
		})
	}
	if data.Data == nil {
		data.Data = []LogCategoryTotal{}
	}

	return data, nil
}
