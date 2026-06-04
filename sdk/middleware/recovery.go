package middleware

import (
	"log"
	"runtime/debug"

	"hei-gin/sdk/exception"
	"hei-gin/sdk/result"

	"github.com/gin-gonic/gin"
)

// Recovery returns a Gin middleware that catches panics and converts them
// into structured JSON responses.
//
// Panic handling:
//   - *exception.BusinessError → 200 with business code and message (no stack trace logged)
//   - Any other panic          → 500 with "服务器内部错误" (full stack trace logged)
//
// NOTE: Must be outermost middleware. Use with gin.New(), NOT gin.Default().
func Recovery() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if rec := recover(); rec != nil {
				switch e := rec.(type) {
				case *exception.BusinessError:
					// Business errors: no stack trace, just return JSON
					c.JSON(200, result.Failure(c, e.Message, e.Code, nil))
				case error:
					log.Printf("[PANIC] %v\n%s", e, string(debug.Stack()))
					c.JSON(200, result.Failure(c, "服务器内部错误", 500, nil))
				default:
					log.Printf("[PANIC] %v\n%s", rec, string(debug.Stack()))
					c.JSON(200, result.Failure(c, "服务器内部错误", 500, nil))
				}
				c.Abort()
			}
		}()

		c.Next()

		if err := c.Errors.Last(); err != nil {
			c.JSON(200, result.Failure(c, err.Error(), 400, nil))
			c.Abort()
		}
	}
}

// SafeCall executes fn with panic recovery, returning any error.
// BusinessError panics are converted to errors; other panics are re-panicked
// to be caught by the top-level Recovery middleware.
//
// Usage:
//
//	err := SafeCall(func() {
//	    riskyOperation()
//	})
func SafeCall(fn func()) (err error) {
	defer func() {
		if rec := recover(); rec != nil {
			switch e := rec.(type) {
			case *exception.BusinessError:
				err = e
			default:
				// Re-panic non-business errors to be caught by top-level Recovery
				panic(rec)
			}
		}
	}()
	fn()
	return nil
}

// SafeCallCtx is like SafeCall but passes a context-aware function.
func SafeCallCtx(fn func(ctx *gin.Context) error, c *gin.Context) (err error) {
	defer func() {
		if rec := recover(); rec != nil {
			switch e := rec.(type) {
			case *exception.BusinessError:
				err = e
			default:
				panic(rec)
			}
		}
	}()
	return fn(c)
}
