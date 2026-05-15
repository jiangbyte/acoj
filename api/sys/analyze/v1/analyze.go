package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type DashboardReq struct {
	g.Meta `path:"/api/v1/sys/analyze/dashboard" method:"get" summary:"获取仪表盘数据" tags:"数据分析"`
}

type DashboardRes struct {
	TotalUsers   int          `json:"total_users"`
	ActiveUsers  int          `json:"active_users"`
	TotalRoles   int          `json:"total_roles"`
	TotalOrgs    int          `json:"total_orgs"`
	TotalConfigs int          `json:"total_configs"`
	TotalNotices int          `json:"total_notices"`
	UserTrend    []MonthItem  `json:"user_trend"`
	ClientStats  []ClientItem `json:"client_stats"`
	SysInfo      SysInfoItem  `json:"sys_info"`
}

type MonthItem struct {
	Month string `json:"month"`
	Count int    `json:"count"`
}

type ClientItem struct {
	Client string `json:"client"`
	Count  int    `json:"count"`
}

type SysInfoItem struct {
	Os string `json:"os"`
	Ip string `json:"ip"`
}
