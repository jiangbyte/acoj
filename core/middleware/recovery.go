package middleware

import (
	"hei-gin/core/exception"
	"hei-gin/core/result"

	"github.com/gin-gonic/gin"
)

// Recovery returns a Gin middleware that catches panics and collects gin errors,
// converting them into structured JSON responses.
//
// Panic handling:
//   - *exception.BusinessError (or exception.BusinessError) -> 200 with the business code and message
//   - Any other panic                                     -> 500 with "服务器内部错误"
//
// Post-handler error handling:
//   - c.Errors.Last() is checked after the handler chain runs and returned as a 400 error
func Recovery() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if rec := recover(); rec != nil {
				switch e := rec.(type) {
				case *exception.BusinessError:
					c.JSON(200, result.Failure(c, e.Message, e.Code, nil))
				case exception.BusinessError:
					c.JSON(200, result.Failure(c, e.Message, e.Code, nil))
				default:
					c.JSON(200, result.Failure(c, "服务器内部错误", 500, nil))
				}
				c.Abort()
			}
		}()

		c.Next()

		// Handle gin errors (e.g. validation errors) that were
		// collected via c.Error() during the handler chain.
		if err := c.Errors.Last(); err != nil {
			c.JSON(200, result.Failure(c, err.Error(), 400, nil))
			c.Abort()
		}
	}
}
