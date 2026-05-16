package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type SysQuickAction struct {
	ent.Schema
}

func (SysQuickAction) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_quick_action"},
	}
}

func (SysQuickAction) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("user_id").MaxLen(32).NotEmpty().Comment("用户ID"),
		field.String("resource_id").MaxLen(32).NotEmpty().Comment("资源ID"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}

func (SysQuickAction) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("user_id", "resource_id").Unique(),
	}
}
