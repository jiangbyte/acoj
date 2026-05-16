package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/index"
)

type SysUser struct {
	ent.Schema
}

func (SysUser) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_user"},
	}
}

func (SysUser) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("username").MaxLen(32).Optional().Nillable().Comment("账号"),
		field.String("password").MaxLen(255).Optional().Nillable().Comment("密码"),
		field.String("nickname").MaxLen(32).Optional().Nillable().Comment("昵称"),
		field.Text("avatar").Optional().Nillable().Comment("头像"),
		field.String("motto").MaxLen(32).Optional().Nillable().Comment("座右铭"),
		field.String("gender").MaxLen(8).Optional().Nillable().Comment("性别"),
		field.Time("birthday").Optional().Nillable().SchemaType(map[string]string{"mysql": "date"}).Comment("生日"),
		field.String("email").MaxLen(64).Optional().Nillable().Comment("电子邮箱"),
		field.String("github").MaxLen(64).Optional().Nillable().Comment("GitHub"),
		field.String("phone").MaxLen(32).Optional().Nillable().Comment("手机号"),
		field.String("org_id").MaxLen(32).Optional().Nillable().Comment("所属组织ID"),
		field.String("position_id").MaxLen(32).Optional().Nillable().Comment("所属职位ID"),
		field.String("group_id").MaxLen(32).Optional().Nillable().Comment("所属用户组ID"),
		field.String("status").MaxLen(16).Optional().Default("ACTIVE").Comment("状态"),
		field.Time("last_login_at").Optional().Nillable().Comment("最后登录时间"),
		field.String("last_login_ip").MaxLen(64).Optional().Nillable().Comment("最后登录IP"),
		field.Int("login_count").Optional().Default(0).Comment("登录次数"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}

func (SysUser) Indexes() []ent.Index {
	return []ent.Index{
		index.Fields("username"),
	}
}
