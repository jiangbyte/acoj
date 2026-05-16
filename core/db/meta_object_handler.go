package db

import (
	"context"
	"time"

	"hei-gin/core/utils"
)

// MetaObjectHandler defines hooks for auto-populating entity fields
// before insert/update operations, similar to MyBatis-Plus MetaObjectHandler.
type MetaObjectHandler struct {
	IDGenerator func() string
}

// DefaultMetaObjectHandler returns a MetaObjectHandler with Snowflake ID generation.
func DefaultMetaObjectHandler() *MetaObjectHandler {
	return &MetaObjectHandler{
		IDGenerator: func() string {
			return utils.NextID()
		},
	}
}

// InsertFill populates standard audit fields for a new entity.
// Fields: id, created_at, updated_at, created_by.
func (h *MetaObjectHandler) InsertFill(ctx context.Context, entity interface {
	SetID(string)
	SetCreatedAt(time.Time)
	SetUpdatedAt(time.Time)
	SetCreatedBy(string)
}, userID string) {
	now := time.Now()
	if h.IDGenerator != nil {
		id := h.IDGenerator()
		if id != "" {
			entity.SetID(id)
		}
	}
	entity.SetCreatedAt(now)
	entity.SetUpdatedAt(now)
	if userID != "" {
		entity.SetCreatedBy(userID)
	}
}

// UpdateFill populates audit fields for an update operation.
// Fields: updated_at, updated_by.
func (h *MetaObjectHandler) UpdateFill(ctx context.Context, entity interface {
	SetUpdatedAt(time.Time)
	SetUpdatedBy(string)
}, userID string) {
	entity.SetUpdatedAt(time.Now())
	if userID != "" {
		entity.SetUpdatedBy(userID)
	}
}
