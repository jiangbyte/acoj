package db

import (
	"hei-gin/core/pojo"
)

// Paginate creates a paginated result from records and total count.
// This is a generic helper used by service layer code.
//
// Usage:
//
//	total := db.Client.SysUser.Query().Count(ctx)
//	users := db.Client.SysUser.Query().Offset(page.Offset()).Limit(page.Size).All(ctx)
//	result := db.Paginate(users, total, page)
func Paginate[T any](records []T, total int, bounds *pojo.PageBounds) *pojo.PageResult[T] {
	if bounds == nil {
		bounds = &pojo.PageBounds{Current: 1, Size: 10}
	} else {
		if bounds.Current <= 0 {
			bounds.Current = 1
		}
		if bounds.Size <= 0 {
			bounds.Size = 10
		}
	}
	return pojo.NewPageResult(records, int64(total), bounds.Current, bounds.Size)
}
