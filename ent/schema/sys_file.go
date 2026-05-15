package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
)

type SysFile struct {
	ent.Schema
}

func (SysFile) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).Comment("主键"),
		field.String("name").MaxLen(255).Comment("文件名"),
		field.String("original_name").MaxLen(255).Comment("原始文件名"),
		field.String("path").MaxLen(500).Comment("存储路径"),
		field.String("url").MaxLen(500).Comment("访问URL"),
		field.String("mime_type").MaxLen(128).Optional().Comment("MIME类型"),
		field.Int64("size").Optional().Default(0).Comment("文件大小(字节)"),
		field.String("category").MaxLen(64).Optional().Comment("文件类别"),
		field.String("storage").MaxLen(32).Optional().Default("local").Comment("存储方式"),
		field.Time("created_at").Optional().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Comment("创建用户"),
		field.Time("updated_at").Optional().Comment("更新时间"),
		field.String("updated_by").MaxLen(32).Optional().Comment("更新用户"),
	}
}

func (SysFile) Edges() []ent.Edge {
	return nil
}
