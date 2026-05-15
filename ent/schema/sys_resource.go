package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysResource struct {
	ent.Schema
}

func (SysResource) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("资源名称"),
		field.String("code").MaxLen(255).Unique().Comment("资源编码"),
		field.String("parent_id").MaxLen(32).Optional().Comment("父级ID"),
		field.String("hierarchy").MaxLen(500).Optional().Comment("层级路径"),
		field.String("type").MaxLen(32).Optional().Default("MENU").Comment("资源类型"),
		field.String("category").MaxLen(32).Optional().Default("BACKEND_MENU").Comment("资源类别"),
		field.String("icon").MaxLen(128).Optional().Comment("图标"),
		field.String("path").MaxLen(500).Optional().Comment("路由路径"),
		field.String("component").MaxLen(500).Optional().Comment("组件路径"),
		field.String("permission").MaxLen(255).Optional().Comment("权限标识"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Bool("visible").Optional().Default(true).Comment("是否可见"),
		field.Bool("keep_alive").Optional().Default(false).Comment("是否缓存"),
		field.Bool("is_frame").Optional().Default(false).Comment("是否外链"),
		field.Bool("is_cache").Optional().Default(true).Comment("是否缓存"),
		field.Bool("is_affix").Optional().Default(false).Comment("是否固定"),
		field.Text("description").Optional().Comment("资源描述"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysResource) Edges() []ent.Edge {
	return nil
}
