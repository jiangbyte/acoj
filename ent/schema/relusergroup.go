package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelUserGroup struct {
	ent.Schema
}

func (RelUserGroup) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "rel_user_group"},
	}
}

func (RelUserGroup) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("user_id").MaxLen(32).NotEmpty().Comment("用户ID"),
		field.String("group_id").MaxLen(32).NotEmpty().Comment("用户组ID"),
	}
}

func (RelUserGroup) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("user_id", "group_id").Unique(),
		index.Fields("group_id"),
	}
}
