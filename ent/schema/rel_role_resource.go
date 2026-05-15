package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelRoleResource struct {
	ent.Schema
}

func (RelRoleResource) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("role_id").MaxLen(32).Comment("角色ID"),
		field.String("resource_id").MaxLen(32).Comment("资源ID"),
	}
}

func (RelRoleResource) Edges() []ent.Edge {
	return nil
}

func (RelRoleResource) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("role_id", "resource_id").Unique(),
	}
}
