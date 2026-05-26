package log

import (
	"context"
	"log"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	logModel "hei-gin/modules/sys/log"

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

	record := logModel.SysLog{
		ID:         utils.GenerateID(),
		Category:   &category,
		Name:       &name,
		ExeStatus:  &exeStatus,
		ExeMessage: &exeMsg,
		OpIP:       &opIP,
		OpAddress:  &cityInfo,
		OpBrowser:  &browser,
		OpOs:       &osName,
		ReqMethod:  strPtr(c.Request.Method),
		ReqURL:     strPtr(c.Request.URL.String()),
		TraceID:    &traceID,
		SignData:   &signData,
		OpUser:     &opUser,
		OpTime:     &now,
		CreatedAt:  &now,
		UpdatedAt:  &now,
	}

	if err := db.DB.WithContext(ctx).Create(&record).Error; err != nil {
		log.Printf("[AUDIT] Failed to persist auth log: %v", err)
	}
}

func strPtr(s string) *string { return &s }
