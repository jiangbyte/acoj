package utility

// PageReq is the common page query request.
type PageReq struct {
	Current int `json:"current"`
	Size    int `json:"size"`
}

// PageRes is the common page query response.
type PageRes struct {
	Records interface{} `json:"records"`
	Total   int         `json:"total"`
	Page    int         `json:"page"`
	Size    int         `json:"size"`
	Pages   int         `json:"pages"`
}

// NewPageRes creates a PageRes with calculated pages.
func NewPageRes(records interface{}, total, page, size int) *PageRes {
	pages := total / size
	if total%size > 0 {
		pages++
	}
	return &PageRes{
		Records: records,
		Total:   total,
		Page:    page,
		Size:    size,
		Pages:   pages,
	}
}
