package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysPermission struct {
	ent.Schema
}

func (SysPermission) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("权限名称"),
		field.String("code").MaxLen(255).Unique().Comment("权限编码"),
		field.String("category").MaxLen(32).Optional().Default("BACKEND").Comment("权限类别"),
		field.Text("description").Optional().Comment("权限描述"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysPermission) Edges() []ent.Edge {
	return nil
}
