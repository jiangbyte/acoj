package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysConfig struct {
	ent.Schema
}

func (SysConfig) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("config_key").MaxLen(255).Unique().Comment("配置键"),
		field.String("config_value").MaxLen(2000).Comment("配置值"),
		field.String("category").MaxLen(64).Optional().Comment("配置类别"),
		field.String("name").MaxLen(255).Optional().Comment("配置名称"),
		field.Text("description").Optional().Comment("配置描述"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.String("type").MaxLen(32).Optional().Comment("配置类型"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysConfig) Edges() []ent.Edge {
	return nil
}
