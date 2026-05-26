package result

import (
	"hei-gin/core/enums"

	"github.com/gin-gonic/gin"
)

func getTraceID(c *gin.Context) string {
	if c == nil { return "" }
	v, exists := c.Get("trace_id")
	if !exists { return "" }
	s, ok := v.(string)
	if !ok { return "" }
	return s
}

func Success(c *gin.Context, data any) gin.H {
	return gin.H{"code": 200, "message": "请求成功", "data": data, "success": true, "trace_id": getTraceID(c)}
}

func Failure(c *gin.Context, message string, code int, data any) gin.H {
	return gin.H{"code": code, "message": message, "data": data, "success": false, "trace_id": getTraceID(c)}
}

type PageData struct {
	Records any   `json:"records"`
	Total   int64 `json:"total"`
	Page    int   `json:"page"`
	Size    int   `json:"size"`
	Pages   int   `json:"pages"`
}

func PageDataResult(c *gin.Context, records any, total int64, page, size int) gin.H {
	pages := 0
	if size > 0 {
		pages = int((total + int64(size) - 1) / int64(size))
	}
	return gin.H{
		"code":    200,
		"message": "请求成功",
		"data": gin.H{
			string(enums.PageDataFieldRecords): records,
			string(enums.PageDataFieldTotal):   total,
			string(enums.PageDataFieldPage):    page,
			string(enums.PageDataFieldSize):    size,
			string(enums.PageDataFieldPages):   pages,
		},
		"success":  true,
		"trace_id": getTraceID(c),
	}
}
