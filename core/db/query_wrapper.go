package db

import (
	"entgo.io/ent/dialect/sql"
)

// QueryWrapper provides helper utilities for building Ent queries.
// Ent's generated query builders are already type-safe and fluent.
// This offers convenience functions for pagination and sorting.
type QueryWrapper struct{}

// NewQueryWrapper creates a new QueryWrapper.
func NewQueryWrapper() *QueryWrapper {
	return &QueryWrapper{}
}

// SortOrder represents a sort direction for a field.
type SortOrder struct {
	Field string
	Desc  bool
}

// ApplyPagination applies offset and limit to an Ent sql.Selector.
func ApplyPagination(selector *sql.Selector, offset, limit int) {
	selector.Offset(offset)
	if limit > 0 {
		selector.Limit(limit)
	}
}

// ApplySort applies ordering to an Ent query selector.
func ApplySort(selector *sql.Selector, orders ...SortOrder) {
	for _, o := range orders {
		if o.Desc {
			selector.OrderBy(sql.Desc(o.Field))
		} else {
			selector.OrderBy(sql.Asc(o.Field))
		}
	}
}
