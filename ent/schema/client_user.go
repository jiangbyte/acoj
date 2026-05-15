package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type ClientUser struct {
	ent.Schema
}

func (ClientUser) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("account").MaxLen(255).Unique().Comment("用户名"),
		field.String("password").MaxLen(255).Comment("密码(bcrypt)"),
		field.String("nickname").MaxLen(255).Optional().Comment("昵称"),
		field.String("avatar").MaxLen(500).Optional().Comment("头像URL"),
		field.String("email").MaxLen(128).Optional().Comment("电子邮箱"),
		field.String("phone").MaxLen(32).Optional().Comment("手机号码"),
		field.String("status").MaxLen(16).Optional().Default("ACTIVE").Comment("用户状态"),
		field.String("gender").MaxLen(8).Optional().Default("UNKNOWN").Comment("性别"),
		field.Time("birthday").Optional().Comment("出生日期"),
		field.Text("description").Optional().Comment("用户描述"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (ClientUser) Edges() []ent.Edge {
	return nil
}
