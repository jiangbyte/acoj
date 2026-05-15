package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelUserRole struct {
	ent.Schema
}

func (RelUserRole) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("user_id").MaxLen(32).Comment("用户ID"),
		field.String("role_id").MaxLen(32).Comment("角色ID"),
	}
}

func (RelUserRole) Edges() []ent.Edge {
	return nil
}

func (RelUserRole) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("user_id", "role_id").Unique(),
	}
}
