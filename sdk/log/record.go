package log

import (
	"log"
	"time"

	"hei-gin/sdk/utils"

	"github.com/gin-gonic/gin"
)

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

	signData := GenerateLogSignature(map[string]any{
		"category":    category,
		"name":        name,
		"exe_status":  exeStatus,
		"exe_message": exeMessage,
		"params":      "",
		"op_time":     now.Format("2006-01-02 15:04:05"),
	})

	if LogPersistence != nil {
		LogPersistence(nil, category, name, exeStatus, exeMessage, opIP, cityInfo, browser, osName, opUser, traceID, signData, c.Request.Method, c.Request.URL.String(), "", now)
	} else {
		log.Printf("[AUDIT] LogPersistence not set, skipping auth log")
	}
}
