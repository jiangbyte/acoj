package log

import (
	"context"
	"fmt"
	"log"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/utils"
	logModel "hei-gin/modules/sys/log"

	"github.com/gin-gonic/gin"
)

func SysLog(name string) gin.HandlerFunc {
	return func(c *gin.Context) {
		startTime := time.Now()
		paramsJSON := ExtractParamsJson(c)

		var category string
		var exeStatus string
		var exeMessage string

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

func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, paramsJSON string, startTime time.Time) {
	userAgent := c.GetHeader("User-Agent")
	browser, osName := ParseUserAgent(userAgent)
	opIP := utils.GetClientIP(c)
	cityInfo := utils.GetCityInfo(opIP)
	traceID := utils.GetTraceID(c)

	opUserStr, exists := c.Get("loginUser")
	opUser, ok := opUserStr.(string)
	if !exists || !ok || opUserStr == "" {
		opUser = "-"
	}

	ctx := context.Background()
	now := time.Now()

	exeMsg := exeMessage
	params := paramsJSON

	signData := GenerateLogSignature(map[string]any{
		"category":    category,
		"name":        name,
		"exe_status":  exeStatus,
		"exe_message": exeMessage,
		"params":      params,
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
		ParamJSON:  &params,
		OpTime:     &now,
		TraceID:    &traceID,
		SignData:   &signData,
		OpUser:     &opUser,
		CreatedAt:  &now,
		UpdatedAt:  &now,
	}

	if err := db.DB.WithContext(ctx).Create(&record).Error; err != nil {
		log.Printf("[SYSLOG] Failed to persist operation log: %v", err)
	}
}
