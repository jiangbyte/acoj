package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type RelRolePermission struct {
	ent.Schema
}

func (RelRolePermission) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("role_id").MaxLen(32).Comment("角色ID"),
		field.String("permission_code").MaxLen(255).Comment("权限编码"),
		field.String("scope").MaxLen(32).Optional().Default("ALL").Comment("数据范围"),
		field.Text("custom_scope_group_ids").Optional().Comment("自定义分组IDs"),
		field.Text("custom_scope_org_ids").Optional().Comment("自定义组织IDs"),
	}
}

func (RelRolePermission) Edges() []ent.Edge {
	return nil
}

func (RelRolePermission) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("role_id", "permission_code").Unique(),
	}
}
