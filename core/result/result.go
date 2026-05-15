package result

import (
	"github.com/gin-gonic/gin"
)

type Response struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
	Success bool        `json:"success"`
	TraceID string      `json:"trace_id"`
}

type PageData struct {
	Records interface{} `json:"records"`
	Total   int64       `json:"total"`
	Page    int         `json:"page"`
	Size    int         `json:"size"`
	Pages   int         `json:"pages"`
}

func Success(c *gin.Context, data interface{}) {
	c.JSON(200, Response{
		Code:    200,
		Message: "请求成功",
		Data:    data,
		Success: true,
		TraceID: GetTraceID(c),
	})
}

func Failure(c *gin.Context, message string, code int) {
	c.JSON(200, Response{
		Code:    code,
		Message: message,
		Data:    nil,
		Success: false,
		TraceID: GetTraceID(c),
	})
}

func Page(c *gin.Context, records interface{}, total int64, page, size int) {
	pages := 0
	if size > 0 {
		pages = (int(total) + size - 1) / size
	}
	Success(c, PageData{
		Records: records,
		Total:   total,
		Page:    page,
		Size:    size,
		Pages:   pages,
	})
}

func GetTraceID(c *gin.Context) string {
	if id, exists := c.Get("trace_id"); exists {
		return id.(string)
	}
	return ""
}
