package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysConfig struct {
	ent.Schema
}

func (SysConfig) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_config"},
	}
}

func (SysConfig) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("config_key").MaxLen(255).Optional().Nillable().Comment("配置键"),
		field.Text("config_value").Optional().Nillable().Comment("配置值"),
		field.String("category").MaxLen(255).Optional().Nillable().Comment("分类"),
		field.String("remark").MaxLen(500).Optional().Nillable().Comment("备注"),
		field.Int("sort_code").Optional().Default(0).Comment("排序码"),
		field.Text("extra").Optional().Nillable().Comment("扩展信息"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("修改时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("修改用户"),
	}
}
