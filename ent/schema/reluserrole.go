package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelUserRole struct {
	ent.Schema
}

func (RelUserRole) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "rel_user_role"},
	}
}

func (RelUserRole) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("user_id").MaxLen(32).NotEmpty().Comment("用户ID"),
		field.String("role_id").MaxLen(32).NotEmpty().Comment("角色ID"),
	}
}

func (RelUserRole) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("user_id", "role_id").Unique(),
		index.Fields("role_id"),
	}
}
