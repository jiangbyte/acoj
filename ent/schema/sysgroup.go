package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysGroup struct {
	ent.Schema
}

func (SysGroup) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_group"},
	}
}

func (SysGroup) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("code").MaxLen(32).NotEmpty().Unique().Comment("用户组编码"),
		field.String("name").MaxLen(64).NotEmpty().Comment("用户组名称"),
		field.String("category").MaxLen(32).NotEmpty().Comment("用户组类别"),
		field.String("parent_id").MaxLen(32).Optional().Nillable().Comment("父用户组ID"),
		field.String("org_id").MaxLen(32).NotEmpty().Comment("所属组织ID"),
		field.String("description").MaxLen(500).Optional().Nillable().Comment("用户组描述"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Text("extra").Optional().Nillable().Comment("扩展信息"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}
