package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysLog struct {
	ent.Schema
}

func (SysLog) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_log"},
	}
}

func (SysLog) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("category").MaxLen(255).Optional().Nillable().Comment("日志分类"),
		field.String("name").MaxLen(255).Optional().Nillable().Comment("日志名称"),
		field.String("exe_status").MaxLen(255).Optional().Nillable().Comment("执行状态"),
		field.Text("exe_message").Optional().Nillable().Comment("具体消息"),
		field.String("op_ip").MaxLen(255).Optional().Nillable().Comment("操作ip"),
		field.String("op_address").MaxLen(255).Optional().Nillable().Comment("操作地址"),
		field.String("op_browser").MaxLen(255).Optional().Nillable().Comment("操作浏览器"),
		field.String("op_os").MaxLen(255).Optional().Nillable().Comment("操作系统"),
		field.String("class_name").MaxLen(255).Optional().Nillable().Comment("类名称"),
		field.String("method_name").MaxLen(255).Optional().Nillable().Comment("方法名称"),
		field.String("req_method").MaxLen(255).Optional().Nillable().Comment("请求方式"),
		field.Text("req_url").Optional().Nillable().Comment("请求地址"),
		field.Text("param_json").Optional().Nillable().Comment("请求参数"),
		field.Text("result_json").Optional().Nillable().Comment("返回结果"),
		field.Time("op_time").Optional().Nillable().Comment("操作时间"),
		field.String("trace_id").MaxLen(64).Optional().Nillable().Comment("跟踪ID"),
		field.String("op_user").MaxLen(255).Optional().Nillable().Comment("操作人姓名"),
		field.Text("sign_data").Optional().Nillable().Comment("签名数据"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("更新用户"),
	}
}
