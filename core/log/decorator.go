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
		if db.RawDB == nil {
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

		// Check if there was a panic/abort with business error
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

		opTime := int(time.Since(start).Milliseconds())
		traceID := result.GetTraceID(c)
		ip := utils.GetClientIP(c)

		// Save log entry
		saveLog(c, name, category, exeStatus, exeMessage, loginID, traceID, ip, string(reqBody), string(respBody), opTime)
	}
}

func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, loginID, traceID, ip, paramJSON, resultJSON string, opTime int) {
	ctx := context.Background()
	now := time.Now()

	_, err := db.RawDB.ExecContext(ctx,
		`INSERT INTO sys_log (id, category, name, exe_status, exe_message, trace_id, op_ip,
		 req_method, req_url, param_json, result_json, op_time, op_user, created_at, created_by)
		 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
		utils.NextID(),
		category,
		name,
		exeStatus,
		exeMessage,
		traceID,
		ip,
		c.Request.Method,
		c.Request.URL.String(),
		paramJSON,
		resultJSON,
		opTime,
		loginID,
		now,
		loginID,
	)
	if err != nil {
		log.Printf("[SysLog] insert error: %v", err)
	}
}
