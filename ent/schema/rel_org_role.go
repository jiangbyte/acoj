package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelOrgRole struct {
	ent.Schema
}

func (RelOrgRole) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("org_id").MaxLen(32).Comment("组织ID"),
		field.String("role_id").MaxLen(32).Comment("角色ID"),
		field.String("scope").MaxLen(32).Optional().Default("ALL").Comment("数据范围"),
		field.Text("custom_scope_group_ids").Optional().Comment("自定义分组IDs"),
	}
}

func (RelOrgRole) Edges() []ent.Edge {
	return nil
}

func (RelOrgRole) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("org_id", "role_id").Unique(),
	}
}
