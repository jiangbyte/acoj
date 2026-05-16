package pojo

// PageBounds represents pagination parameters.
type PageBounds struct {
	Current int `json:"current"`
	Size    int `json:"size"`
}

func (p *PageBounds) Offset() int {
	if p.Current <= 0 {
		p.Current = 1
	}
	if p.Size <= 0 {
		p.Size = 10
	}
	return (p.Current - 1) * p.Size
}

// PageResult is the generic pagination result.
type PageResult[T any] struct {
	Records []T   `json:"records"`
	Total   int64 `json:"total"`
	Page    int   `json:"page"`
	Size    int   `json:"size"`
	Pages   int   `json:"pages"`
}

func NewPageResult[T any](records []T, total int64, page, size int) *PageResult[T] {
	pages := 0
	if size > 0 {
		pages = (int(total) + size - 1) / size
	}
	return &PageResult[T]{
		Records: records,
		Total:   total,
		Page:    page,
		Size:    size,
		Pages:   pages,
	}
}

// IdParam is a request parameter carrying a single ID.
type IdParam struct {
	ID string `json:"id" form:"id" binding:"required"`
}

// IdsParam is a request parameter carrying a list of IDs.
type IdsParam struct {
	IDs []string `json:"ids" form:"ids" binding:"required"`
}
