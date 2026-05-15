package v1

import (
	"github.com/gogf/gf/v2/frame/g"
)

type DashboardReq struct {
	g.Meta `path:"/api/v1/sys/analyze/dashboard" method:"get" summary:"获取仪表盘数据" tags:"数据分析"`
}

type DashboardStatsItem struct {
	TotalUsers   int `json:"total_users"`
	ActiveUsers  int `json:"active_users"`
	TotalRoles   int `json:"total_roles"`
	TotalOrgs    int `json:"total_orgs"`
	TotalConfigs int `json:"total_configs"`
	TotalNotices int `json:"total_notices"`
}

type DashboardRes struct {
	Stats                    DashboardStatsItem `json:"stats"`
	UserTrend                []MonthItem        `json:"user_trend"`
	ClientStats              ClientStatsItem    `json:"client_stats"`
	ClientTrend              []MonthItem        `json:"client_trend"`
	OrgUserDistribution      []OrgDistItem      `json:"org_user_distribution"`
	RoleCategoryDistribution []CategoryItem     `json:"role_category_distribution"`
	SysInfo                  SysInfoItem        `json:"sys_info"`
}

type MonthItem struct {
	Month string `json:"month"`
	Count int    `json:"count"`
}

type ClientStatsItem struct {
	TotalUsers  int `json:"total_users"`
	ActiveUsers int `json:"active_users"`
}

type OrgDistItem struct {
	Name  string `json:"name"`
	Count int    `json:"count"`
}

type CategoryItem struct {
	Category string `json:"category"`
	Count    int    `json:"count"`
}

type SysInfoItem struct {
	GoVersion string `json:"go_version"`
	OsName    string `json:"os_name"`
	ServerIp  string `json:"server_ip"`
	RunTime   string `json:"run_time"`
}
