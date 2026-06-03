package session

type SessionAnalysisResult struct {
	TotalCount        int    `json:"total_count"`
	MaxTokenCount     int    `json:"max_token_count"`
	OneHourNewlyAdded int    `json:"one_hour_newly_added"`
	ProportionOfBAndC string `json:"proportion_of_b_and_c"`
}

type SessionPageResult struct {
	UserID                string  `json:"user_id"`
	Username              *string `json:"username"`
	Nickname              *string `json:"nickname"`
	Avatar                *string `json:"avatar"`
	Status                string  `json:"status"`
	LastLoginIP           *string `json:"last_login_ip"`
	LastLoginAddress      *string `json:"last_login_address"`
	LastLoginTime         string  `json:"last_login_time"`
	SessionCreateTime     string  `json:"session_create_time"`
	SessionTimeout        string  `json:"session_timeout"`
	SessionTimeoutSeconds int     `json:"session_timeout_seconds"`
	TokenCount            int     `json:"token_count"`
}

type SessionExitParam struct {
	UserID string `json:"user_id"`
}

type SessionExitTokenParam struct {
	UserID string `json:"user_id"`
	Token  string `json:"token"`
}

type SessionTokenResult struct {
	Token          string `json:"token"`
	CreatedAt      string `json:"created_at"`
	Timeout        string `json:"timeout"`
	TimeoutSeconds int    `json:"timeout_seconds"`
	DeviceType     string `json:"device_type"`
	DeviceID       string `json:"device_id"`
}

type SessionPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword" form:"keyword"`
}

type SessionChartData struct {
	BarChart BarChartData `json:"bar_chart"`
	PieChart PieChartData `json:"pie_chart"`
}

type BarChartData struct {
	Days   []string         `json:"days"`
	Series []CategorySeries `json:"series"`
}

type PieChartData struct {
	Data []CategoryTotal `json:"data"`
}

type CategorySeries struct {
	Name string `json:"name"`
	Data []int  `json:"data"`
}

type CategoryTotal struct {
	Category string `json:"category"`
	Total    int    `json:"total"`
}
