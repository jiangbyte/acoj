package log

import (
	"log"
	"time"

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

		c.Next()

		var category string
		var exeStatus string
		var exeMessage string

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
//
// Stub: currently logs to console with log.Printf since the DB/ent model
// for sys_log does not exist yet. Replace with real persistence once
// the log module is implemented.
func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, paramsJSON string, startTime time.Time) {
	userAgent := c.GetHeader("User-Agent")
	browser, osName := ParseUserAgent(userAgent)
	opIP := utils.GetClientIP(c)
	cityInfo := utils.GetCityInfo(opIP)
	traceID := utils.GetTraceID()

	// Try to get operator user from Gin context (set by auth middleware)
	opUser, exists := c.Get("loginUser")
	opUserStr, ok := opUser.(string)
	if !exists || !ok || opUserStr == "" {
		opUserStr = "-"
	}

	elapsed := time.Since(startTime)

	log.Printf("[SYSLOG] name=%s category=%s status=%s message=%s trace=%s ip=%s city=%s browser=%s os=%s user=%s elapsed=%v params=%s",
		name, category, exeStatus, exeMessage, traceID, opIP, cityInfo, browser, osName, opUserStr, elapsed, paramsJSON)
}
