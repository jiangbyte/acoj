package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysBanner struct {
	ent.Schema
}

func (SysBanner) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("title").MaxLen(255).Comment("轮播标题"),
		field.String("image").MaxLen(500).Comment("轮播图片"),
		field.String("category").MaxLen(32).Comment("轮播类别"),
		field.String("type").MaxLen(32).Comment("轮播类型"),
		field.String("position").MaxLen(32).Comment("展示位置"),
		field.String("url").MaxLen(500).Optional().Comment("跳转地址"),
		field.String("link_type").MaxLen(16).Optional().Default("URL").Comment("链接类型"),
		field.String("summary").MaxLen(500).Optional().Comment("轮播摘要"),
		field.Text("description").Optional().Comment("轮播描述"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Int("view_count").Optional().Default(0).Comment("浏览次数"),
		field.Int("click_count").Optional().Default(0).Comment("点击次数"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysBanner) Edges() []ent.Edge {
	return nil
}
