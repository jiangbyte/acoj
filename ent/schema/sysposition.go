package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysPosition struct {
	ent.Schema
}

func (SysPosition) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_position"},
	}
}

func (SysPosition) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("code").MaxLen(32).NotEmpty().Unique().Comment("职位编码"),
		field.String("name").MaxLen(64).NotEmpty().Comment("职位名称"),
		field.String("category").MaxLen(32).NotEmpty().Comment("职位类别"),
		field.String("org_id").MaxLen(32).Optional().Nillable().Comment("所属组织ID"),
		field.String("group_id").MaxLen(32).Optional().Nillable().Comment("所属用户组ID"),
		field.String("description").MaxLen(500).Optional().Nillable().Comment("职位描述"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Text("extra").Optional().Nillable().Comment("扩展信息"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}
