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

// BaseExportParam is the common export request parameter matching the Python BaseExportParam.
type BaseExportParam struct {
	ExportType string `json:"export_type"` // "current", "selected", "all"
	Current    int    `json:"current"`
	Size       int    `json:"size"`
	SelectedId string `json:"selected_id"` // comma-separated IDs
}

// SelectedIds parses the comma-separated selected_id into a slice.
func (p *BaseExportParam) SelectedIds() []string {
	if p.SelectedId == "" {
		return nil
	}
	return SplitIds(p.SelectedId)
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
