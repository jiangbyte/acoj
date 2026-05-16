package log

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"strings"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/result"
	"hei-gin/core/utils"
)

// parseUserAgent extracts browser and OS from a User-Agent string.
func parseUserAgent(ua string) (browser, os string) {
	ua = strings.ToLower(ua)
	switch {
	case strings.Contains(ua, "edg"):
		browser = "Edge"
	case strings.Contains(ua, "chrome"):
		browser = "Chrome"
	case strings.Contains(ua, "safari"):
		browser = "Safari"
	case strings.Contains(ua, "firefox"):
		browser = "Firefox"
	case strings.Contains(ua, "opera") || strings.Contains(ua, "opr"):
		browser = "Opera"
	case strings.Contains(ua, "msie") || strings.Contains(ua, "trident"):
		browser = "IE"
	default:
		browser = "Unknown"
	}

	switch {
	case strings.Contains(ua, "windows"):
		os = "Windows"
	case strings.Contains(ua, "mac os") || strings.Contains(ua, "macintosh"):
		os = "macOS"
	case strings.Contains(ua, "linux"):
		os = "Linux"
	case strings.Contains(ua, "android"):
		os = "Android"
	case strings.Contains(ua, "iphone") || strings.Contains(ua, "ipad"):
		os = "iOS"
	default:
		os = "Unknown"
	}
	return
}

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

		opTime := int(time.Since(start).Milliseconds())
		traceID := result.GetTraceID(c)
		ip := utils.GetClientIP(c)

		// Parse User-Agent
		userAgent := c.GetHeader("User-Agent")
		browser, osName := parseUserAgent(userAgent)

		// Get handler info
		handlerName := c.HandlerName()

		saveLog(c, name, category, exeStatus, exeMessage, loginID, traceID, ip, userAgent,
			browser, osName, handlerName, c.Request.Method, c.Request.URL.String(),
			string(reqBody), string(respBody), opTime)
	}
}

func saveLog(c *gin.Context, name, category, exeStatus, exeMessage, loginID, traceID, ip, userAgent,
	browser, osName, handlerName, reqMethod, reqURL, paramJSON, resultJSON string, opTime int) {

	// Build signature
	signInput := fmt.Sprintf("%s|%s|%s|%s|%s|%s", category, name, exeStatus, ip, time.Now().Format(time.RFC3339), traceID)
	signData := fmt.Sprintf("%x", sha256.Sum256([]byte(signInput)))

	ctx := context.Background()
	now := time.Now()

	_, err := db.Client.SysLog.Create().
		SetID(utils.NextID()).
		SetCategory(category).
		SetName(name).
		SetExeStatus(exeStatus).
		SetExeMessage(exeMessage).
		SetTraceID(traceID).
		SetOpIP(ip).
		SetOpAddress("").
		SetOpBrowser(browser).
		SetOpOs(osName).
		SetClassName(handlerName).
		SetMethodName(c.HandlerName()).
		SetReqMethod(reqMethod).
		SetReqURL(reqURL).
		SetParamJSON(paramJSON).
		SetResultJSON(resultJSON).
		SetOpTime(opTime).
		SetOpUser(loginID).
		SetSignData(signData).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		Save(ctx)
	if err != nil {
		log.Printf("[SysLog] insert error: %v", err)
	}
}
