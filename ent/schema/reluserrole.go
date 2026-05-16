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
		field.String("scope").MaxLen(32).Optional().Nillable().Comment("数据范围覆盖：ALL-全部，CUSTOM-自定义，ORG-本组织，ORG_AND_BELOW-本组织及以下，SELF-本人。为空则继承 rel_role_permission 的配置"),
		field.Text("custom_scope_group_ids").Optional().Nillable().Comment("自定义数据范围组ID列表(JSON数组)，scope=CUSTOM时生效"),
	}
}

func (RelUserRole) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("user_id", "role_id").Unique(),
		index.Fields("role_id"),
	}
}
