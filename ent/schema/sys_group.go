package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysGroup struct {
	ent.Schema
}

func (SysGroup) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("分组名称"),
		field.String("code").MaxLen(255).Unique().Comment("分组编码"),
		field.String("parent_id").MaxLen(32).Optional().Comment("父级ID"),
		field.String("hierarchy").MaxLen(500).Optional().Comment("层级路径"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Text("description").Optional().Comment("分组描述"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysGroup) Edges() []ent.Edge {
	return nil
}
