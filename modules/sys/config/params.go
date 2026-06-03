package config

type ConfigVO struct {
	ID          string  `json:"id"`
	ConfigKey   *string `json:"config_key"`
	ConfigValue *string `json:"config_value"`
	Category    *string `json:"category"`
	Remark      *string `json:"remark"`
	SortCode    int     `json:"sort_code"`
	Extra       *string `json:"extra"`
}

type ConfigPageParam struct {
	Current  int    `json:"current" form:"current"`
	Size     int    `json:"size" form:"size"`
	Category string `json:"category" form:"category"`
	Keyword  string `json:"keyword" form:"keyword"`
}

type ConfigListParam struct {
	Category string `json:"category" form:"category"`
}
type ConfigBatchEditItem struct {
	ID          string  `json:"id"`
	ConfigKey   *string `json:"config_key"`
	ConfigValue *string `json:"config_value"`
	Remark      *string `json:"remark"`
	SortCode    int     `json:"sort_code"`
}
type ConfigBatchEditParam struct {
	Configs []ConfigBatchEditItem `json:"configs"`
}
type ConfigCategoryEditParam struct {
	Category    string  `json:"category"`
	ConfigKey   *string `json:"config_key"`
	ConfigValue *string `json:"config_value"`
	Remark      *string `json:"remark"`
}
