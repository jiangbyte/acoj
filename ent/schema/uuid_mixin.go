package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/schema/field"
	"entgo.io/ent/schema/mixin"
	"github.com/google/uuid"
)

type UUIDMixin struct {
	mixin.Schema
}

func (UUIDMixin) Fields() []ent.Field {
	return []ent.Field{
		field.String("id").
			MaxLen(36).
			DefaultFunc(func() string {
				return uuid.Must(uuid.NewV7()).String()
			}).
			Comment("主键"),
	}
}
