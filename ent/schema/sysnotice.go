package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type SysNotice struct {
	ent.Schema
}

func (SysNotice) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_notice"},
	}
}

func (SysNotice) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("title").MaxLen(255).NotEmpty().Comment("通知标题"),
		field.String("summary").MaxLen(500).Optional().Nillable().Comment("通知摘要"),
		field.Text("content").Optional().Nillable().Comment("通知内容"),
		field.String("cover").MaxLen(500).Optional().Nillable().Comment("封面图片"),
		field.String("category").MaxLen(32).NotEmpty().Comment("通知类别"),
		field.String("type").MaxLen(32).NotEmpty().Comment("通知类型"),
		field.String("level").MaxLen(16).Optional().Default("NORMAL").Comment("通知级别"),
		field.Int("view_count").Optional().Default(0).Comment("浏览次数"),
		field.String("is_top").MaxLen(8).Optional().Default("NO").Comment("是否置顶"),
		field.String("position").MaxLen(32).Optional().Nillable().Comment("通知位置"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}

func (SysNotice) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("category", "type"),
		index.Fields("status"),
	}
}
