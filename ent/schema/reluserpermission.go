package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelUserPermission struct {
	ent.Schema
}

func (RelUserPermission) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "rel_user_permission"},
	}
}

func (RelUserPermission) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("user_id").MaxLen(32).NotEmpty().Comment("用户ID"),
		field.String("permission_code").MaxLen(255).NotEmpty().Comment("权限编码"),
		field.String("scope").MaxLen(32).Optional().Default("ALL").Comment("数据范围：ALL-全部，SELF-本人，ORG-本组织，ORG_AND_BELOW-本组织及以下，CUSTOM_ORG-自定义组织，GROUP-本用户组，GROUP_AND_BELOW-本用户组及以下，CUSTOM_GROUP-自定义用户组"),
		field.Text("custom_scope_group_ids").Optional().Nillable().Comment("自定义用户组ID列表(JSON数组)，scope=CUSTOM_GROUP时生效"),
		field.Text("custom_scope_org_ids").Optional().Nillable().Comment("自定义组织ID列表(JSON数组)，scope=CUSTOM_ORG时生效"),
	}
}

func (RelUserPermission) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("user_id", "permission_code").Unique(),
		index.Fields("permission_code"),
	}
}
