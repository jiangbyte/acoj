package utility

import (
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
)

// DefaultHandlerResponse is the default response structure.
type DefaultHandlerResponse struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data"`
	Success bool        `json:"success"`
	TraceID string      `json:"trace_id"`
}

// Success returns a success response.
func Success(data ...interface{}) g.Map {
	result := g.Map{
		"code":     200,
		"message":  "请求成功",
		"data":     nil,
		"success":  true,
		"trace_id": "",
	}
	if len(data) > 0 {
		result["data"] = data[0]
	}
	return result
}

// Failure returns a failure response.
func Failure(message string, code ...int) g.Map {
	c := 400
	if len(code) > 0 {
		c = code[0]
	}
	return g.Map{
		"code":     c,
		"message":  message,
		"data":     nil,
		"success":  false,
		"trace_id": "",
	}
}

// MiddlewareHandlerResponse is the default response middleware for GoFrame.
func MiddlewareHandlerResponse(r *ghttp.Request) {
	r.Middleware.Next()
	if r.Response.BufferLength() > 0 {
		return
	}
	var data = r.GetHandlerResponse()
	if err := r.GetError(); err != nil {
		r.Response.WriteJson(Failure(err.Error()))
		return
	}
	r.Response.WriteJson(Success(data))
}
