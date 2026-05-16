package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
)

type SysFile struct {
	ent.Schema
}

func (SysFile) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "sys_file"},
	}
}

func (SysFile) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").MaxLen(32).NotEmpty().Comment("主键"),
		field.String("engine").MaxLen(50).Optional().Nillable().Comment("存储引擎"),
		field.String("bucket").MaxLen(255).Optional().Nillable().Comment("存储桶"),
		field.String("file_key").MaxLen(500).Optional().Nillable().Comment("文件Key"),
		field.Text("name").Optional().Nillable().Comment("文件名称"),
		field.String("suffix").MaxLen(50).Optional().Nillable().Comment("文件后缀"),
		field.Int64("size_kb").Optional().Nillable().Comment("文件大小kb"),
		field.String("size_info").MaxLen(50).Optional().Nillable().Comment("文件大小（格式化后）"),
		field.Text("obj_name").Optional().Nillable().Comment("文件的对象名（唯一名称）"),
		field.Text("storage_path").Optional().Nillable().Comment("文件存储路径"),
		field.Text("download_path").Optional().Nillable().Comment("文件下载路径"),
		field.Bool("is_download_auth").Optional().Nillable().Comment("文件下载是否需要授权"),
		field.Text("thumbnail").Optional().Nillable().Comment("图片缩略图"),
		field.Text("extra").Optional().Nillable().Comment("扩展信息"),
		field.Time("created_at").Optional().Nillable().Comment("创建时间"),
		field.String("created_by").MaxLen(32).Optional().Nillable().Comment("创建用户"),
		field.Time("updated_at").Optional().Nillable().Comment("修改时间"),
		field.String("updated_by").MaxLen(32).Optional().Nillable().Comment("修改用户"),
	}
}
