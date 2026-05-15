package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysRole struct {
	ent.Schema
}

func (SysRole) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("角色名称"),
		field.String("code").MaxLen(255).Unique().Comment("角色编码"),
		field.String("data_scope").MaxLen(32).Optional().Default("ALL").Comment("数据权限范围"),
		field.Text("custom_scope_org_ids").Optional().Comment("自定义组织权限IDs"),
		field.Text("custom_scope_group_ids").Optional().Comment("自定义分组权限IDs"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Text("description").Optional().Comment("角色描述"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysRole) Edges() []ent.Edge {
	return nil
}
