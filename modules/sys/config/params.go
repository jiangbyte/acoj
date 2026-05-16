package config

// ConfigVO is the view object for a system config, used for create/modify requests and API responses.
type ConfigVO struct {
	ID          string  `json:"id,omitempty"`
	ConfigKey   string  `json:"config_key,omitempty"`
	ConfigValue string  `json:"config_value,omitempty"`
	Category    string  `json:"category,omitempty"`
	Remark      *string `json:"remark,omitempty"`
	SortCode    int     `json:"sort_code,omitempty"`
	Extra       *string `json:"ext_json,omitempty"`
}

// ConfigPageParam holds pagination parameters for the config page query.
type ConfigPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	Category string `json:"category,omitempty" form:"category"`
	Keyword  string `json:"keyword,omitempty" form:"keyword"`
}

// ConfigListParam holds the category filter for listing configs.
type ConfigListParam struct {
	Category string `json:"category" form:"category"`
}

// ConfigBatchEditParam holds the batch edit request body.
type ConfigBatchEditParam struct {
	Configs []ConfigVO `json:"configs"`
}

// ConfigCategoryEditParam holds the category-based batch edit request body.
type ConfigCategoryEditParam struct {
	Category string     `json:"category"`
	Configs  []ConfigVO `json:"configs"`
}
