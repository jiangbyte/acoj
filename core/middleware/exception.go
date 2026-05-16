package middleware

import (
	"log"
	"runtime/debug"

	"github.com/gin-gonic/gin"

	bizerr "hei-gin/core/exception"
	"hei-gin/core/result"
)

// ExceptionHandler returns a Gin middleware that recovers from panics
// and returns structured JSON responses, matching fastapi's setup_exception_handlers.
//
// BusinessError panics are returned as HTTP 200 with the error's code/message.
// Unexpected panics are returned as HTTP 200 with a generic 500 message.
func ExceptionHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if err := recover(); err != nil {
				switch e := err.(type) {
				case *bizerr.BusinessError:
					// BusinessError → 200 with business error code
					c.JSON(200, result.Response{
						Code:    e.Code,
						Message: e.Message,
						Data:    nil,
						Success: false,
						TraceID: result.GetTraceID(c),
					})
				case error:
					// Generic error → 200 with 500
					log.Printf("[Exception] %v\n%s", e, string(debug.Stack()))
					c.JSON(200, result.Response{
						Code:    500,
						Message: "服务器内部错误",
						Data:    nil,
						Success: false,
						TraceID: result.GetTraceID(c),
					})
				default:
					// Non-error panic → 200 with 500
					log.Printf("[Exception] %v\n%s", e, string(debug.Stack()))
					c.JSON(200, result.Response{
						Code:    500,
						Message: "服务器内部错误",
						Data:    nil,
						Success: false,
						TraceID: result.GetTraceID(c),
					})
				}
				c.Abort()
			}
		}()
		c.Next()
	}
}
