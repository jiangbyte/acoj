package v1

import (
	"github.com/gogf/gf/v2/frame/g"
	"hei-goframe/utility"
)

// --- Analysis ---

type SessionAnalysisReq struct {
	g.Meta `path:"/api/v1/sys/session/analysis" method:"get" summary:"会话分析" tags:"会话管理"`
}

type SessionAnalysisRes struct {
	TotalCount        int    `json:"total_count"`
	MaxTokenCount     int    `json:"max_token_count"`
	OneHourNewlyAdded int    `json:"one_hour_newly_added"`
	ProportionBC      string `json:"proportion_of_b_and_c"`
}

// --- Page ---

type SessionPageReq struct {
	g.Meta  `path:"/api/v1/sys/session/page" method:"get" summary:"分页查询会话" tags:"会话管理"`
	Keyword string `json:"keyword"`
	utility.PageReq
}

type SessionPageRes struct {
	utility.PageRes
}

// --- Exit (kickout) ---

type SessionExitReq struct {
	g.Meta `path:"/api/v1/sys/session/exit" method:"post" summary:"踢下线" tags:"会话管理"`
	UserId string `json:"user_id" v:"required#用户ID不能为空"`
}

type SessionExitRes struct{}

// --- Tokens ---

type SessionTokensReq struct {
	g.Meta `path:"/api/v1/sys/session/tokens" method:"get" summary:"获取用户token列表" tags:"会话管理"`
	UserId string `json:"user_id" v:"required#用户ID不能为空"`
}

type SessionTokenItem struct {
	Token          string `json:"token"`
	CreatedAt      string `json:"created_at"`
	Timeout        string `json:"timeout"`
	TimeoutSeconds int    `json:"timeout_seconds"`
	DeviceType     string `json:"device_type"`
	DeviceId       string `json:"device_id"`
}

type SessionTokensRes []SessionTokenItem

// --- Exit Token ---

type SessionExitTokenReq struct {
	g.Meta `path:"/api/v1/sys/session/exit-token" method:"post" summary:"踢下线指定token" tags:"会话管理"`
	UserId string `json:"user_id" v:"required#用户ID不能为空"`
	Token  string `json:"token" v:"required#Token不能为空"`
}

type SessionExitTokenRes struct{}

// --- Chart Data ---

type SessionChartDataReq struct {
	g.Meta `path:"/api/v1/sys/session/chart-data" method:"get" summary:"会话图表数据" tags:"会话管理"`
}

type SessionChartSeriesItem struct {
	Name string `json:"name"`
	Data []int  `json:"data"`
}

type SessionChartPieItem struct {
	Category string `json:"category"`
	Total    int    `json:"total"`
}

type BarChartData struct {
	Days   []string                 `json:"days"`
	Series []SessionChartSeriesItem `json:"series"`
}

type PieChartData struct {
	Data []SessionChartPieItem `json:"data"`
}

type SessionChartDataRes struct {
	BarChart *BarChartData `json:"bar_chart"`
	PieChart *PieChartData `json:"pie_chart"`
}
