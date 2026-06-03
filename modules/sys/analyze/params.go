package analyze

type TrendItem struct {
	Month string `json:"month"`
	Count int    `json:"count"`
}

type OrgUserDistribution struct {
	Name  string `json:"name"`
	Count int    `json:"count"`
}

type CategoryDistribution struct {
	Category string `json:"category"`
	Count    int    `json:"count"`
}

type DashboardStats struct {
	TotalUsers   int64 `json:"total_users"`
	ActiveUsers  int64 `json:"active_users"`
	TotalRoles   int64 `json:"total_roles"`
	TotalOrgs    int64 `json:"total_orgs"`
	TotalConfigs int64 `json:"total_configs"`
	TotalNotices int64 `json:"total_notices"`
}

type SysInfo struct {
	OsName   string `json:"os_name"`
	ServerIP string `json:"server_ip"`
	RunTime  string `json:"run_time"`
}

type ClientStats struct {
	TotalUsers  int64 `json:"total_users"`
	ActiveUsers int64 `json:"active_users"`
}

type DashboardVO struct {
	Stats                    DashboardStats         `json:"stats"`
	ClientStats              ClientStats            `json:"client_stats"`
	UserTrend                []TrendItem            `json:"user_trend"`
	ClientTrend              []TrendItem            `json:"client_trend"`
	OrgUserDistribution      []OrgUserDistribution  `json:"org_user_distribution"`
	RoleCategoryDistribution []CategoryDistribution `json:"role_category_distribution"`
	SysInfo                  SysInfo                `json:"sys_info"`
}

// LogAnalysisData holds login/log analysis statistics for the dashboard.
type LogAnalysisData struct {
	LoginTotal     int `json:"login_total"`
	LoginFailed    int `json:"login_failed"`
	LoginToday     int `json:"login_today"`
	LogTotal       int `json:"log_total"`
	LogException   int `json:"log_exception"`
	ExceptionToday int `json:"exception_today"`
}
