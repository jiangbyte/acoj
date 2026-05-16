package log

import (
	"bytes"
	"context"
	"encoding/json"
	"io"
	"log"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/result"
	"hei-gin/core/utils"
)

type bodyLogWriter struct {
	gin.ResponseWriter
	body *bytes.Buffer
}

func (w *bodyLogWriter) Write(b []byte) (int, error) {
	w.body.Write(b)
	return w.ResponseWriter.Write(b)
}

// SysLog returns a Gin middleware that records an operation log entry.
func SysLog(name string) gin.HandlerFunc {
	return func(c *gin.Context) {
		if db.Client == nil {
			c.Next()
			return
		}

		// Capture request body
		var reqBody []byte
		if c.Request.Body != nil {
			reqBody, _ = io.ReadAll(c.Request.Body)
			c.Request.Body = io.NopCloser(bytes.NewBuffer(reqBody))
		}

		// Wrap response writer
		blw := &bodyLogWriter{body: &bytes.Buffer{}, ResponseWriter: c.Writer}
		c.Writer = blw

		start := time.Now()

		c.Next()

		// Determine status from response code
		category := "OPERATE"
		exeStatus := "SUCCESS"
		exeMessage := ""

		if c.Writer.Status() >= 400 || len(c.Errors) > 0 {
			exeStatus = "FAIL"
			if len(c.Errors) > 0 {
				exeMessage = c.Errors.Last().Err.Error()
			}
		}

		// Parse response to check business code
		respBody := blw.body.Bytes()
		if len(respBody) > 0 {
			var respMap map[string]interface{}
			if json.Unmarshal(respBody, &respMap) == nil {
				if code, ok := respMap["code"].(float64); ok && code >= 400 {
					exeStatus = "FAIL"
					if msg, ok := respMap["message"].(string); ok {
						exeMessage = msg
					}
				}
			}
		}

		// Get current user
		loginType := auth.DetectLoginType(c)
		loginID := ""
		if loginType == "CONSUMER" {
			loginID = auth.ClientAuthTool.GetLoginID(c)
		} else {
			loginID = auth.AuthTool.GetLoginID(c)
		}

		traceID := result.GetTraceID(c)
		ip := utils.GetClientIP(c)

		// Parse User-Agent
		userAgent := c.GetHeader("User-Agent")
		browser, osName := ParseUserAgent(userAgent)

		// Get handler info
		handlerName := c.HandlerName()

		saveLog(c, name, category, exeStatus, exeMessage, loginID, traceID, ip, userAgent,
			browser, osName, handlerName, c.Request.Method, c.Request.URL.String(),
			string(reqBody), string(respBody), start)
	}
}

func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, loginID, traceID, ip, userAgent,
	browser, osName, handlerName, reqMethod, reqURL, paramJSON, resultJSON string, startTime time.Time) {

	ctx := context.Background()
	now := time.Now()

	// Build log signature (matching fastapi's generate_log_signature)
	signData := GenerateLogSignature(map[string]interface{}{
		"category":   category,
		"name":       name,
		"exe_status": exeStatus,
		"op_ip":      ip,
		"op_time":    now.Format(time.RFC3339),
		"op_user":    loginID,
		"trace_id":   traceID,
	})

	_, err := db.Client.SysLog.Create().
		SetID(utils.NextID()).
		SetCategory(category).
		SetName(name).
		SetExeStatus(exeStatus).
		SetExeMessage(exeMessage).
		SetTraceID(traceID).
		SetOpIP(ip).
		SetOpAddress(utils.GetCityInfo(ip)).
		SetOpBrowser(browser).
		SetOpOs(osName).
		SetClassName(handlerName).
		SetMethodName(c.HandlerName()).
		SetReqMethod(reqMethod).
		SetReqURL(reqURL).
		SetParamJSON(paramJSON).
		SetResultJSON(resultJSON).
		SetOpTime(startTime).
		SetOpUser(loginID).
		SetSignData(signData).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		Save(ctx)
	if err != nil {
		log.Printf("[SysLog] insert error: %v", err)
	}
}
