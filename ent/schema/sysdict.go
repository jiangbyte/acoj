package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysDict struct {
	ent.Schema
}

func (SysDict) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_dict"},
	}
}

func (SysDict) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("code").MaxLen(50).NotEmpty().Unique().Comment("字典编码"),
		field.String("label").MaxLen(255).Optional().Nillable().Comment("字典标签"),
		field.String("value").MaxLen(255).Optional().Nillable().Comment("字典值"),
		field.String("color").MaxLen(32).Optional().Nillable().Comment("字典颜色"),
		field.String("category").MaxLen(64).Optional().Nillable().Comment("字典分类"),
		field.String("parent_id").MaxLen(32).Optional().Nillable().Comment("父字典ID"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}
