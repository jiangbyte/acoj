package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelRoleResource struct {
	ent.Schema
}

func (RelRoleResource) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "rel_role_resource"},
	}
}

func (RelRoleResource) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("role_id").MaxLen(32).NotEmpty().Comment("角色ID"),
		field.String("resource_id").MaxLen(32).NotEmpty().Comment("资源ID"),
	}
}

func (RelRoleResource) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("role_id", "resource_id").Unique(),
		index.Fields("resource_id"),
	}
}
