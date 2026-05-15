package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysResource = sysResourceDao{}

type sysResourceDao struct {
	Table   string
	Columns sysResourceColumns
}

type sysResourceColumns struct {
	Id            string
	Code          string
	Name          string
	Category      string
	Type          string
	Description   string
	ParentId      string
	RoutePath     string
	ComponentPath string
	RedirectPath  string
	Icon          string
	Color         string
	IsVisible     string
	IsCache       string
	IsAffix       string
	IsBreadcrumb  string
	ExternalUrl   string
	Extra         string
	Status        string
	SortCode      string
	CreatedAt     string
	CreatedBy     string
	UpdatedAt     string
	UpdatedBy     string
}

func init() {
	SysResource.Table = "sys_resource"
	SysResource.Columns = sysResourceColumns{
		Id:            "id",
		Code:          "code",
		Name:          "name",
		Category:      "category",
		Type:          "type",
		Description:   "description",
		ParentId:      "parent_id",
		RoutePath:     "route_path",
		ComponentPath: "component_path",
		RedirectPath:  "redirect_path",
		Icon:          "icon",
		Color:         "color",
		IsVisible:     "is_visible",
		IsCache:       "is_cache",
		IsAffix:       "is_affix",
		IsBreadcrumb:  "is_breadcrumb",
		ExternalUrl:   "external_url",
		Extra:         "extra",
		Status:        "status",
		SortCode:      "sort_code",
		CreatedAt:     "created_at",
		CreatedBy:     "created_by",
		UpdatedAt:     "updated_at",
		UpdatedBy:     "updated_by",
	}
}

func (d sysResourceDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}
