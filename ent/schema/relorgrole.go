package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelOrgRole struct {
	ent.Schema
}

func (RelOrgRole) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "rel_org_role"},
	}
}

func (RelOrgRole) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("org_id").MaxLen(32).NotEmpty().Comment("组织ID"),
		field.String("role_id").MaxLen(32).NotEmpty().Comment("角色ID"),
		field.String("scope").MaxLen(32).Optional().Nillable().Comment("数据范围覆盖：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组。为空则继承 rel_role_permission 的配置"),
		field.Text("custom_scope_group_ids").Optional().Nillable().Comment("自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效"),
		field.Text("custom_scope_org_ids").Optional().Nillable().Comment("自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效"),
	}
}

func (RelOrgRole) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("org_id", "role_id").Unique(),
		index.Fields("role_id"),
	}
}
