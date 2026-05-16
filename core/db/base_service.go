package db

import (
	"hei-gin/core/pojo"
)

// BaseService provides reusable CRUD patterns for Ent models.
// Embed this struct in concrete service implementations.
type BaseService struct {
	MetaObjectHandler *MetaObjectHandler
}

// NewBaseService creates a BaseService with the default MetaObjectHandler.
func NewBaseService() *BaseService {
	return &BaseService{
		MetaObjectHandler: DefaultMetaObjectHandler(),
	}
}

// PageResult creates a paginated result from records and total count.
func PageResult[T any](records []T, total int, bounds *pojo.PageBounds) *pojo.PageResult[T] {
	return Paginate(records, total, bounds)
}
