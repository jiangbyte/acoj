package log

import (
	"log"
	"time"

	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// RecordAuthLog records an auth-related log (login/logout) programmatically.
//
// Unlike the SysLog middleware, this does not need a function context and
// accepts the operator name directly — which is essential for login events
// where there is no active auth token yet.
//
// Stub: currently logs to console with log.Printf. Replace with DB
// persistence once the log module is implemented.
func RecordAuthLog(c *gin.Context, name, category, exeStatus, exeMessage, opUser string) {
	now := time.Now()
	userAgent := c.GetHeader("User-Agent")
	browser, osName := ParseUserAgent(userAgent)
	opIP := utils.GetClientIP(c)
	cityInfo := utils.GetCityInfo(opIP)
	traceID := utils.GetTraceID()

	if opUser == "" {
		opUser = "-"
	}

	log.Printf("[AUTHLOG] name=%s category=%s status=%s message=%s trace=%s ip=%s city=%s browser=%s os=%s user=%s time=%v",
		name, category, exeStatus, exeMessage, traceID, opIP, cityInfo, browser, osName, opUser, now)
}
