package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysNotice struct {
	ent.Schema
}

func (SysNotice) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("title").MaxLen(255).Comment("公告标题"),
		field.Text("content").Optional().Comment("公告内容"),
		field.String("category").MaxLen(32).Optional().Comment("公告类别"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Time("publish_time").Optional().Comment("发布时间"),
		field.Time("expire_time").Optional().Comment("过期时间"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysNotice) Edges() []ent.Edge {
	return nil
}
