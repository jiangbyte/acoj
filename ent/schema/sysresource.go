package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysResource struct {
	ent.Schema
}

func (SysResource) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_resource"},
	}
}

func (SysResource) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("code").MaxLen(32).NotEmpty().Unique().Comment("资源编码"),
		field.String("name").MaxLen(64).NotEmpty().Comment("资源名称"),
		field.String("category").MaxLen(16).NotEmpty().Comment("资源分类：BACKEND_MENU-后台菜单，FRONTEND_MENU-前台菜单，BACKEND_BUTTON-后台按钮，FRONTEND_BUTTON-前台按钮"),
		field.String("type").MaxLen(16).NotEmpty().Comment("资源类型：DIRECTORY-目录，MENU-菜单，BUTTON-按钮，INTERNAL_LINK-内链，EXTERNAL_LINK-外链"),
		field.String("description").MaxLen(500).Optional().Nillable().Comment("资源描述"),
		field.String("parent_id").MaxLen(32).Optional().Nillable().Comment("父资源ID"),
		field.String("route_path").MaxLen(255).Optional().Nillable().Comment("路由路径"),
		field.String("component_path").MaxLen(255).Optional().Nillable().Comment("组件路径"),
		field.String("redirect_path").MaxLen(255).Optional().Nillable().Comment("重定向路径"),
		field.String("icon").MaxLen(64).Optional().Nillable().Comment("资源图标"),
		field.String("color").MaxLen(32).Optional().Nillable().Comment("资源颜色（前台资源使用）"),
		field.String("is_visible").MaxLen(8).Optional().Default("YES").Comment("是否可见"),
		field.String("is_cache").MaxLen(8).Optional().Default("NO").Comment("是否缓存"),
		field.String("is_affix").MaxLen(8).Optional().Default("NO").Comment("是否固定"),
		field.String("is_breadcrumb").MaxLen(8).Optional().Default("YES").Comment("是否显示面包屑"),
		field.String("external_url").MaxLen(500).Optional().Nillable().Comment("外链地址"),
		field.Text("extra").Optional().Nillable().Comment("扩展信息"),
		field.String("status").MaxLen(16).Optional().Default("ENABLED").Comment("状态"),
		field.Int("sort_code").Optional().Default(0).Comment("排序"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}
