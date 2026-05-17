package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type SysOrg struct {
	ent.Schema
}

func (SysOrg) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_org"},
	}
}

func (SysOrg) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("code").MaxLen(32).NotEmpty().Unique().Comment("组织编码"),
		field.String("name").MaxLen(64).NotEmpty().Comment("组织名称"),
		field.String("category").MaxLen(32).NotEmpty().Comment("组织类别"),
		field.String("parent_id").MaxLen(32).Optional().Nillable().Comment("父组织ID"),
		field.String("description").MaxLen(500).Optional().Nillable().Comment("组织描述"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Text("extra").Optional().Nillable().Comment("扩展信息"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}

func (SysOrg) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("parent_id"),
	}
}
