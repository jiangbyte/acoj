package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysLog struct {
	ent.Schema
}

func (SysLog) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("category").MaxLen(32).Optional().Comment("日志类别(OPERATE/EXCEPTION)"),
		field.String("name").MaxLen(255).Optional().Comment("操作名称"),
		field.String("exe_status").MaxLen(16).Optional().Comment("执行状态(SUCCESS/FAIL)"),
		field.Text("exe_message").Optional().Comment("执行消息"),
		field.String("trace_id").MaxLen(64).Optional().Comment("追踪ID"),
		field.String("op_ip").MaxLen(64).Optional().Comment("操作IP"),
		field.String("op_address").MaxLen(255).Optional().Comment("操作地点"),
		field.String("op_browser").MaxLen(255).Optional().Comment("操作浏览器"),
		field.String("op_os").MaxLen(255).Optional().Comment("操作系统"),
		field.String("class_name").MaxLen(255).Optional().Comment("类名"),
		field.String("method_name").MaxLen(255).Optional().Comment("方法名"),
		field.String("req_method").MaxLen(16).Optional().Comment("请求方法"),
		field.String("req_url").MaxLen(500).Optional().Comment("请求URL"),
		field.Text("param_json").Optional().Comment("请求参数JSON"),
		field.Text("result_json").Optional().Comment("响应结果JSON"),
		field.Int("op_time").Optional().Default(0).Comment("操作耗时(ms)"),
		field.String("op_user").MaxLen(32).Optional().Comment("操作用户"),
		field.Text("sign_data").Optional().Comment("签名数据"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
	}
}

func (SysLog) Edges() []ent.Edge {
	return nil
}
