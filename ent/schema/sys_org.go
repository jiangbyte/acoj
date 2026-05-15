package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysOrg struct {
	ent.Schema
}

func (SysOrg) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("组织名称"),
		field.String("code").MaxLen(255).Unique().Comment("组织编码"),
		field.String("parent_id").MaxLen(32).Optional().Comment("父级ID"),
		field.String("hierarchy").MaxLen(500).Optional().Comment("层级路径"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Text("description").Optional().Comment("组织描述"),
		field.String("leader").MaxLen(64).Optional().Comment("负责人"),
		field.String("phone").MaxLen(32).Optional().Comment("联系电话"),
		field.String("email").MaxLen(128).Optional().Comment("电子邮箱"),
		field.String("address").MaxLen(500).Optional().Comment("地址"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysOrg) Edges() []ent.Edge {
	return nil
}
