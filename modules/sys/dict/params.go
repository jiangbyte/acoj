package dict

// ---------- Request Parameters ----------

type PageParam struct {
	Page     int    `form:"page" json:"page"`
	Size     int    `form:"size" json:"size"`
	Keyword  string `form:"keyword" json:"keyword"`
	Category string `form:"category" json:"category"`
}

type DictCreateReq struct {
	ParentID    string `json:"parent_id"`   // "0" or "" => create SysDict; otherwise create SysDictData linked to parent
	Code        string `json:"code"`        // SysDict: dictionary code
	Name        string `json:"name"`        // SysDict: dictionary name
	Category    string `json:"category"`    // SysDict: dictionary category
	Description string `json:"description"` // SysDict: dictionary description
	Label       string `json:"label"`       // SysDictData: data label
	Value       string `json:"value"`       // SysDictData: data value
	Color       string `json:"color"`       // SysDictData: display color
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
}

type DictModifyReq struct {
	ID          string `json:"id" binding:"required"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	Description string `json:"description"`
	DictID      string `json:"dict_id"`
	Label       string `json:"label"`
	Value       string `json:"value"`
	Color       string `json:"color"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
}

type RemoveReq struct {
	IDs []string `json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

type GetLabelReq struct {
	TypeCode string `form:"type_code" json:"type_code" binding:"required"`
	Value    string `form:"value" json:"value" binding:"required"`
}

type GetChildrenReq struct {
	TypeCode string `form:"type_code" json:"type_code" binding:"required"`
}

// ---------- Response VOs ----------

// DictVO is used for page / list responses (SysDict entries only).
type DictVO struct {
	ID          string `json:"id"`
	Code        string `json:"code"`
	Name        string `json:"name"`
	Category    string `json:"category"`
	Description string `json:"description"`
	SortCode    int    `json:"sort_code"`
	Status      string `json:"status"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

// DictTreeNode is a unified node for the tree / detail / create / get-children responses.
// The _type field discriminates between "dict" (SysDict) and "data" (SysDictData).
type DictTreeNode struct {
	ID        string          `json:"id"`
	Type      string          `json:"_type"`
	SortCode  int             `json:"sort_code"`
	Status    string          `json:"status"`
	CreatedAt string          `json:"created_at"`
	CreatedBy string          `json:"created_by"`
	UpdatedAt string          `json:"updated_at"`
	UpdatedBy string          `json:"updated_by"`
	Children  []*DictTreeNode `json:"children"`

	// SysDict fields
	Code        string `json:"code,omitempty"`
	Name        string `json:"name,omitempty"`
	Category    string `json:"category,omitempty"`
	Description string `json:"description,omitempty"`

	// SysDictData fields
	DictID string `json:"dict_id,omitempty"`
	Label  string `json:"label,omitempty"`
	Value  string `json:"value,omitempty"`
	Color  string `json:"color,omitempty"`
}

// ---------- Export / Import ----------

var DictExportFieldNames = map[string]string{
	"name":        "字典名称",
	"code":        "字典编码",
	"category":    "字典类别",
	"description": "字典描述",
	"sort_code":   "排序",
	"status":      "状态",
	"created_at":  "创建时间",
}

var DictExportFields = []string{"name", "code", "category", "description", "sort_code", "status", "created_at"}
