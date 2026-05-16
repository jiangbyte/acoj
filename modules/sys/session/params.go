package session

type SessionAnalysisResult struct {
	TotalCount        int    `json:"total_count"`
	MaxTokenCount     int    `json:"max_token_count"`
	OneHourNewlyAdded int    `json:"one_hour_newly_added"`
	ProportionOfBAndC string `json:"proportion_of_b_and_c"`
}

type SessionPageResult struct {
	UserID                string  `json:"user_id,omitempty"`
	Username              *string `json:"username,omitempty"`
	Nickname              *string `json:"nickname,omitempty"`
	Avatar                *string `json:"avatar,omitempty"`
	Status                string  `json:"status,omitempty"`
	LastLoginIP           *string `json:"last_login_ip,omitempty"`
	LastLoginAddress      *string `json:"last_login_address,omitempty"`
	LastLoginTime         string  `json:"last_login_time,omitempty"`
	SessionCreateTime     string  `json:"session_create_time,omitempty"`
	SessionTimeout        string  `json:"session_timeout,omitempty"`
	SessionTimeoutSeconds int     `json:"session_timeout_seconds,omitempty"`
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
	Token          string `json:"token,omitempty"`
	CreatedAt      string `json:"created_at,omitempty"`
	Timeout        string `json:"timeout,omitempty"`
	TimeoutSeconds int    `json:"timeout_seconds,omitempty"`
	DeviceType     string `json:"device_type,omitempty"`
	DeviceID       string `json:"device_id,omitempty"`
}

type SessionPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword,omitempty" form:"keyword"`
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
