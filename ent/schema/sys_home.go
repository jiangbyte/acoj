package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysHome struct {
	ent.Schema
}

func (SysHome) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("首页组件名称"),
		field.Text("config").Optional().Comment("配置JSON"),
		field.String("type").MaxLen(32).Optional().Comment("类型"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysHome) Edges() []ent.Edge {
	return nil
}
