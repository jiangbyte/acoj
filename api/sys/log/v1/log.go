package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Page ---

type LogPageReq struct {
	g.Meta    `path:"/api/v1/sys/log/page" method:"get" summary:"分页查询日志" tags:"操作日志"`
	Keyword   string `json:"keyword"`
	Category  string `json:"category"`
	ExeStatus string `json:"exe_status"`
	utility.PageReq
}

type LogPageRes struct {
	utility.PageRes
}

// --- Create ---

type LogCreateReq struct {
	g.Meta     `path:"/api/v1/sys/log/create" method:"post" summary:"创建日志" tags:"操作日志"`
	Category   string `json:"category"`
	Name       string `json:"name"`
	ExeStatus  string `json:"exe_status"`
	ExeMessage string `json:"exe_message"`
	OpIp       string `json:"op_ip"`
	OpAddress  string `json:"op_address"`
	OpBrowser  string `json:"op_browser"`
	OpOs       string `json:"op_os"`
	ClassName  string `json:"class_name"`
	MethodName string `json:"method_name"`
	ReqMethod  string `json:"req_method"`
	ReqUrl     string `json:"req_url"`
	ParamJson  string `json:"param_json"`
	ResultJson string `json:"result_json"`
	OpTime     string `json:"op_time"`
	TraceId    string `json:"trace_id"`
	OpUser     string `json:"op_user"`
	SignData   string `json:"sign_data"`
}

type LogCreateRes struct{}

// --- Modify ---

type LogModifyReq struct {
	g.Meta     `path:"/api/v1/sys/log/modify" method:"post" summary:"编辑日志" tags:"操作日志"`
	Id         string `json:"id" v:"required#ID不能为空"`
	Category   string `json:"category"`
	Name       string `json:"name"`
	ExeStatus  string `json:"exe_status"`
	ExeMessage string `json:"exe_message"`
	OpIp       string `json:"op_ip"`
	OpAddress  string `json:"op_address"`
	OpBrowser  string `json:"op_browser"`
	OpOs       string `json:"op_os"`
	ClassName  string `json:"class_name"`
	MethodName string `json:"method_name"`
	ReqMethod  string `json:"req_method"`
	ReqUrl     string `json:"req_url"`
	ParamJson  string `json:"param_json"`
	ResultJson string `json:"result_json"`
	OpTime     string `json:"op_time"`
	TraceId    string `json:"trace_id"`
	OpUser     string `json:"op_user"`
	SignData   string `json:"sign_data"`
}

type LogModifyRes struct{}

// --- Remove ---

type LogRemoveReq struct {
	g.Meta `path:"/api/v1/sys/log/remove" method:"post" summary:"删除日志" tags:"操作日志"`
	Ids    []string `json:"ids" v:"required#ID列表不能为空"`
}

type LogRemoveRes struct{}

// --- Detail ---

type LogDetailReq struct {
	g.Meta `path:"/api/v1/sys/log/detail" method:"get" summary:"获取日志详情" tags:"操作日志"`
	Id     string `json:"id" v:"required#ID不能为空"`
}

type LogDetailRes struct {
	Id         string `json:"id"`
	Category   string `json:"category"`
	Name       string `json:"name"`
	ExeStatus  string `json:"exe_status"`
	ExeMessage string `json:"exe_message"`
	OpIp       string `json:"op_ip"`
	OpAddress  string `json:"op_address"`
	OpBrowser  string `json:"op_browser"`
	OpOs       string `json:"op_os"`
	ClassName  string `json:"class_name"`
	MethodName string `json:"method_name"`
	ReqMethod  string `json:"req_method"`
	ReqUrl     string `json:"req_url"`
	ParamJson  string `json:"param_json"`
	ResultJson string `json:"result_json"`
	OpTime     string `json:"op_time"`
	TraceId    string `json:"trace_id"`
	OpUser     string `json:"op_user"`
	SignData   string `json:"sign_data"`
	CreatedAt  string `json:"created_at"`
	CreatedBy  string `json:"created_by"`
	UpdatedAt  string `json:"updated_at"`
	UpdatedBy  string `json:"updated_by"`
}

// --- Delete By Category ---

type LogDeleteByCategoryReq struct {
	g.Meta   `path:"/api/v1/sys/log/delete-by-category" method:"post" summary:"按分类删除日志" tags:"操作日志"`
	Category string `json:"category" v:"required#分类不能为空"`
}

type LogDeleteByCategoryRes struct{}

// --- Export ---

type LogExportReq struct {
	g.Meta     `path:"/api/v1/sys/log/export" method:"get" summary:"导出日志数据" tags:"操作日志"`
	ExportType string `json:"export_type" v:"required#导出类型不能为空"`
	SelectedId string `json:"selected_id"`
	utility.PageReq
}

type LogExportRes struct{}

// --- Template ---

type LogTemplateReq struct {
	g.Meta `path:"/api/v1/sys/log/template" method:"get" summary:"下载日志导入模板" tags:"操作日志"`
}

type LogTemplateRes struct{}

// --- Import ---

type LogImportReq struct {
	g.Meta `path:"/api/v1/sys/log/import" method:"post" summary:"导入日志数据" tags:"操作日志"`
}

type LogImportRes struct {
	Total   int    `json:"total"`
	Message string `json:"message"`
}

// --- Chart shared types ---

type SeriesItem struct {
	Name string `json:"name"`
	Data []int  `json:"data"`
}

type PieChartItem struct {
	Category string `json:"category"`
	Total    int    `json:"total"`
}

// --- Vis Line Chart ---

type LogVisLineChartReq struct {
	g.Meta `path:"/api/v1/sys/log/vis/line-chart-data" method:"get" summary:"访问折线图数据" tags:"操作日志"`
}

type LogVisLineChartRes struct {
	Days   []string     `json:"days"`
	Series []SeriesItem `json:"series"`
}

// --- Vis Pie Chart ---

type LogVisPieChartReq struct {
	g.Meta `path:"/api/v1/sys/log/vis/pie-chart-data" method:"get" summary:"访问饼图数据" tags:"操作日志"`
}

type LogVisPieChartRes struct {
	Data []PieChartItem `json:"data"`
}

// --- Op Bar Chart ---

type LogOpBarChartReq struct {
	g.Meta `path:"/api/v1/sys/log/op/bar-chart-data" method:"get" summary:"操作柱状图数据" tags:"操作日志"`
}

type LogOpBarChartRes struct {
	Days   []string     `json:"days"`
	Series []SeriesItem `json:"series"`
}

// --- Op Pie Chart ---

type LogOpPieChartReq struct {
	g.Meta `path:"/api/v1/sys/log/op/pie-chart-data" method:"get" summary:"操作饼图数据" tags:"操作日志"`
}

type LogOpPieChartRes struct {
	Data []PieChartItem `json:"data"`
}
