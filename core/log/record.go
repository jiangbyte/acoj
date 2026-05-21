package log

import (
	"context"
	"log"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
)

// RecordAuthLog records an auth-related log (login/logout) by persisting to the database.
//
// Unlike the SysLog middleware, this does not need a function context and
// accepts the operator name directly — which is essential for login events
// where there is no active auth token yet.
func RecordAuthLog(c *gin.Context, name, category, exeStatus, exeMessage, opUser string) {
	now := time.Now()
	userAgent := c.GetHeader("User-Agent")
	browser, osName := ParseUserAgent(userAgent)
	opIP := utils.GetClientIP(c)
	cityInfo := utils.GetCityInfo(opIP)
	traceID := utils.GetTraceID(c)

	if opUser == "" {
		opUser = "-"
	}

	ctx := context.Background()
	exeMsg := exeMessage

	signData := GenerateLogSignature(map[string]any{
		"category":    category,
		"name":        name,
		"exe_status":  exeStatus,
		"exe_message": exeMessage,
		"params":      "",
		"op_time":     now.Format("2006-01-02 15:04:05"),
	})

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
		SetTraceID(traceID).
		SetSignData(signData).
		SetOpUser(opUser).
		SetOpTime(now).
		SetCreatedAt(now).
		SetUpdatedAt(now).
		Exec(ctx)
	if err != nil {
		log.Printf("[AUDIT] Failed to persist auth log: %v", err)
	}
}
