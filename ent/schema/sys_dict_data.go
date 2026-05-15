package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysDictData struct {
	ent.Schema
}

func (SysDictData) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("dict_id").MaxLen(32).Comment("字典ID"),
		field.String("label").MaxLen(255).Comment("数据标签"),
		field.String("value").MaxLen(255).Comment("数据值"),
		field.String("color").MaxLen(32).Optional().Comment("颜色"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysDictData) Edges() []ent.Edge {
	return nil
}
