package log

type LogVO struct {
	ID         string `json:"id,omitempty"`
	Category   string `json:"category,omitempty"`
	Name       string `json:"name,omitempty"`
	ExeStatus  string `json:"exe_status,omitempty"`
	ExeMessage string `json:"exe_message,omitempty"`
	OpIP       string `json:"op_ip,omitempty"`
	OpAddress  string `json:"op_address,omitempty"`
	OpBrowser  string `json:"op_browser,omitempty"`
	OpOs       string `json:"op_os,omitempty"`
	ClassName  string `json:"class_name,omitempty"`
	MethodName string `json:"method_name,omitempty"`
	ReqMethod  string `json:"req_method,omitempty"`
	ReqURL     string `json:"req_url,omitempty"`
	ParamJSON  string `json:"param_json,omitempty"`
	ResultJSON string `json:"result_json,omitempty"`
	OpTime     string `json:"op_time,omitempty"`
	TraceID    string `json:"trace_id,omitempty"`
	OpUser     string `json:"op_user,omitempty"`
	SignData   string `json:"sign_data,omitempty"`
	CreatedAt  string `json:"created_at,omitempty"`
	CreatedBy  string `json:"created_by,omitempty"`
	UpdatedAt  string `json:"updated_at,omitempty"`
	UpdatedBy  string `json:"updated_by,omitempty"`
}

type LogPageParam struct {
	Current   int    `json:"current" form:"current"`
	Size      int    `json:"size" form:"size"`
	Keyword   string `json:"keyword,omitempty" form:"keyword"`
	Category  string `json:"category,omitempty" form:"category"`
	ExeStatus string `json:"exe_status,omitempty" form:"exe_status"`
}

type LogDeleteByCategoryParam struct {
	Category string `json:"category"`
}

type BarChartData struct {
	Days   []string         `json:"days"`
	Series []CategorySeries `json:"series"`
}

type CategorySeries struct {
	Name string `json:"name"`
	Data []int  `json:"data"`
}

type PieChartData struct {
	Data []CategoryTotal `json:"data"`
}

type CategoryTotal struct {
	Category string `json:"category"`
	Total    int    `json:"total"`
}
