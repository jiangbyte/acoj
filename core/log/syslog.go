package log

import (
	"context"
	"fmt"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// SysLog returns a Gin middleware that records operation logs.
// It is the Go equivalent of the Python @SysLog(name) decorator.
//
// Usage:
//
//	r.GET("/api/v1/sys/xxx", log.SysLog("日志名称"), handler)
func SysLog(name string) gin.HandlerFunc {
	return func(c *gin.Context) {
		startTime := time.Now()
		paramsJSON := ExtractParamsJson(c)

		var category string
		var exeStatus string
		var exeMessage string

		// Deferred recover: when downstream handler panics, record the log
		// before re-panicking so the Recovery middleware can return the response.
		defer func() {
			if rec := recover(); rec != nil {
				category = "EXCEPTION"
				exeStatus = "FAIL"
				switch e := rec.(type) {
				case *exception.BusinessError:
					exeMessage = fmt.Sprintf("BusinessError{code=%d, message=%s}", e.Code, e.Message)
				case exception.BusinessError:
					exeMessage = fmt.Sprintf("BusinessError{code=%d, message=%s}", e.Code, e.Message)
				default:
					exeMessage = fmt.Sprintf("%v", rec)
				}
				if len(exeMessage) > 2000 {
					exeMessage = exeMessage[:2000]
				}
				saveLog(c, name, category, exeStatus, exeMessage, paramsJSON, startTime)
				panic(rec)
			}
		}()

		c.Next()

		if len(c.Errors) > 0 {
			category = "EXCEPTION"
			exeStatus = "FAIL"
			exeMessage = c.Errors.Last().Error()
			if len(exeMessage) > 2000 {
				exeMessage = exeMessage[:2000]
			}
		} else {
			category = "OPERATE"
			exeStatus = "SUCCESS"
			exeMessage = ""
		}

		saveLog(c, name, category, exeStatus, exeMessage, paramsJSON, startTime)
	}
}

// saveLog persists the log entry to the database.
func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, paramsJSON string, startTime time.Time) {
	userAgent := c.GetHeader("User-Agent")
	browser, osName := ParseUserAgent(userAgent)
	opIP := utils.GetClientIP(c)
	cityInfo := utils.GetCityInfo(opIP)
	traceID := utils.GetTraceID()

	opUserStr, exists := c.Get("loginUser")
	opUser, ok := opUserStr.(string)
	if !exists || !ok || opUserStr == "" {
		opUser = "-"
	}

	ctx := context.Background()
	now := time.Now()

	exeMsg := exeMessage
	params := paramsJSON

	err := db.Client.SysLog.Create().
		SetID(utils.GenerateID()).
		SetCategory(category).
		SetName(name).
		SetExeStatus(exeStatus).
		SetNillableExeMessage(&exeMsg).
		SetOpIP(opIP).
		SetOpAddress(cityInfo).
		SetOpBrowser(browser).
		SetOpOs(osName).
		SetReqMethod(c.Request.Method).
		SetReqURL(c.Request.URL.String()).
		SetNillableParamJSON(&params).
		SetOpTime(now).
		SetTraceID(traceID).
		SetOpUser(opUser).
		SetCreatedAt(now).
		SetUpdatedAt(now).
		Exec(ctx)
	if err != nil {
		// Log failure silently — don't break the request for a logging error
		_ = err
	}
}
