package dao

import (
	"github.com/gogf/gf/v2/database/gdb"
	"github.com/gogf/gf/v2/frame/g"
)

var SysNotice = sysNoticeDao{}

type sysNoticeDao struct {
	Table   string
	Columns sysNoticeColumns
}

type sysNoticeColumns struct {
	Id        string
	Title     string
	Category  string
	Type      string
	Summary   string
	Content   string
	Cover     string
	Level     string
	ViewCount string
	IsTop     string
	Position  string
	Status    string
	SortCode  string
	CreatedAt string
	CreatedBy string
	UpdatedAt string
	UpdatedBy string
}

func init() {
	SysNotice.Table = "sys_notice"
	SysNotice.Columns = sysNoticeColumns{
		Id:        "id",
		Title:     "title",
		Category:  "category",
		Type:      "type",
		Summary:   "summary",
		Content:   "content",
		Cover:     "cover",
		Level:     "level",
		ViewCount: "view_count",
		IsTop:     "is_top",
		Position:  "position",
		Status:    "status",
		SortCode:  "sort_code",
		CreatedAt: "created_at",
		CreatedBy: "created_by",
		UpdatedAt: "updated_at",
		UpdatedBy: "updated_by",
	}
}

func (d sysNoticeDao) Ctx() *gdb.Model {
	return g.DB().Model(d.Table).Safe()
}
