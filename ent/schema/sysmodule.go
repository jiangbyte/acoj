package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysModule struct {
	ent.Schema
}

func (SysModule) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_module"},
	}
}

func (SysModule) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("code").MaxLen(32).NotEmpty().Unique().Comment("模块编码"),
		field.String("name").MaxLen(64).NotEmpty().Comment("模块名称"),
		field.String("category").MaxLen(32).NotEmpty().Comment("模块类别"),
		field.String("icon").MaxLen(64).Optional().Nillable().Comment("模块图标"),
		field.String("color").MaxLen(32).Optional().Nillable().Comment("模块颜色"),
		field.String("description").MaxLen(500).Optional().Nillable().Comment("模块描述"),
		field.String("is_visible").MaxLen(8).Optional().Default("YES").Comment("是否可见"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}
