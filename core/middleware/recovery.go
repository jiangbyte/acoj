package middleware

import (
	"log"
	"runtime/debug"

	"github.com/gin-gonic/gin"

	bizerr "hei-gin/core/errors"
	"hei-gin/core/result"
)

func Recovery() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if err := recover(); err != nil {
				if be, ok := err.(*bizerr.BusinessError); ok {
					c.JSON(200, result.Response{
						Code:    be.Code,
						Message: be.Message,
						Data:    nil,
						Success: false,
						TraceID: result.GetTraceID(c),
					})
				} else {
					log.Printf("[PANIC] %v\n%s", err, string(debug.Stack()))
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
