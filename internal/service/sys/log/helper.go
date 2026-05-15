package log

import (
	"context"
	"encoding/json"
	"time"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"

	"hei-goframe/internal/service/auth"
	"hei-goframe/utility"
)

// SysLog records an operation log entry for the current request.
// Call this at the beginning of controller methods that need logging.
// Example: defer log.SysLog(ctx, "添加轮播图")()
func SysLog(ctx context.Context, name string) func() {
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return func() {}
	}

	startTime := time.Now()
	loginId := auth.GetLoginIdFromCtx(ctx)

	return func() {
		// recover from panic if any
		if rec := recover(); rec != nil {
			doSaveLog(ctx, r, name, "EXCEPTION", "FAIL", fmtError(rec), startTime, loginId)
			panic(rec) // re-panic after logging
		} else {
			doSaveLog(ctx, r, name, "OPERATE", "SUCCESS", "", startTime, loginId)
		}
	}
}

// SysLogSuccess records a successful operation log entry.
func SysLogSuccess(ctx context.Context, name string) {
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return
	}
	loginId := auth.GetLoginIdFromCtx(ctx)
	doSaveLog(ctx, r, name, "OPERATE", "SUCCESS", "", time.Now(), loginId)
}

// SysLogError records a failed operation log entry.
func SysLogError(ctx context.Context, name, errMsg string) {
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return
	}
	loginId := auth.GetLoginIdFromCtx(ctx)
	doSaveLog(ctx, r, name, "EXCEPTION", "FAIL", errMsg, time.Now(), loginId)
}

func doSaveLog(ctx context.Context, r *ghttp.Request, name, category, exeStatus, exeMessage string, startTime time.Time, loginId string) {
	paramsJSON := extractParams(r)
	opUser := resolveOpUser(ctx, loginId)
	opIP := r.GetClientIp()

	_, err := g.DB().Model("sys_log").Ctx(ctx).Insert(g.Map{
		"id":          utility.GenerateID(),
		"category":    category,
		"name":        name,
		"exe_status":  exeStatus,
		"exe_message": exeMessage,
		"op_ip":       opIP,
		"op_browser":  r.Header.Get("User-Agent"),
		"req_method":  r.Method,
		"req_url":     r.GetUrl(),
		"param_json":  paramsJSON,
		"op_time":     startTime.Format("2006-01-02 15:04:05"),
		"op_user":     opUser,
		"created_by":  loginId,
	})
	if err != nil {
		g.Log().Warning(ctx, "Failed to save sys log:", err)
	}
}

func extractParams(r *ghttp.Request) string {
	params := make(map[string]interface{})
	for k, v := range r.GetMap() {
		if k == "request" || k == "db" || k == "file" {
			continue
		}
		params[k] = v
	}
	data, _ := json.Marshal(params)
	return string(data)
}

func resolveOpUser(ctx context.Context, loginId string) string {
	if loginId == "" {
		return ""
	}
	row, err := g.DB().Model("sys_user").Ctx(ctx).WherePri(loginId).Fields("account").One()
	if err != nil || row == nil {
		return ""
	}
	return row["account"].String()
}

func fmtError(v interface{}) string {
	switch e := v.(type) {
	case error:
		return e.Error()
	case string:
		return e
	default:
		return ""
	}
}
