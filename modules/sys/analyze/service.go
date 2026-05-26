package analyze

import (
	"context"
	"log"
	"runtime"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/core/enums"
	"hei-gin/core/db"
	logModel "hei-gin/modules/sys/log"
)

type LogAnalysisData struct {
	LoginTotal     int `json:"login_total"`
	LoginFailed    int `json:"login_failed"`
	LoginToday     int `json:"login_today"`
	LogTotal       int `json:"log_total"`
	LogException   int `json:"log_exception"`
	ExceptionToday int `json:"exception_today"`
}

func Page(c *gin.Context, param *logModel.LogPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}
	if param.Size > 100 {
		param.Size = 100
	}

	query := db.DB.WithContext(ctx).Model(&logModel.SysLog{})
	if param.Category != "" {
		query = query.Where("category = ?", param.Category)
	}
	if param.Keyword != "" {
		kw := "%" + param.Keyword + "%"
		query = query.Where("name LIKE ? OR op_user LIKE ? OR op_ip LIKE ?", kw, kw, kw)
	}

	var total int64
	query.Count(&total)

	var records []logModel.SysLog
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)
	return gin.H{
		"code": 200, "message": "请求成功", "success": true,
		"data": gin.H{
			"records": records, "total": total, "current": param.Current,
			"size": param.Size, "pages": int((total + int64(param.Size) - 1) / int64(param.Size)),
		},
	}
}

func LoginAnalysis(c *gin.Context) *LogAnalysisData {
	ctx := context.Background()

	var loginTotal int64
	db.DB.WithContext(ctx).Model(&logModel.SysLog{}).Where("category = ?", "LOGIN").Count(&loginTotal)

	var failedTotal int64
	db.DB.WithContext(ctx).Model(&logModel.SysLog{}).Where("category = ?", "LOGIN").Where("exe_status = ?", "FAIL").Count(&failedTotal)

	var loginToday int64
	db.DB.WithContext(ctx).Model(&logModel.SysLog{}).
		Where("category = ?", "LOGIN").
		Where("DATE(op_time) = CURDATE()").
		Count(&loginToday)

	log.Printf("[Analyze] Login stats: total=%d, failed=%d, today=%d", loginTotal, failedTotal, loginToday)

	return &LogAnalysisData{
		LoginTotal:  int(loginTotal),
		LoginFailed: int(failedTotal),
		LoginToday:  int(loginToday),
	}
}

func LogAnalysis(c *gin.Context) *LogAnalysisData {
	ctx := context.Background()

	var logTotal int64
	db.DB.WithContext(ctx).Model(&logModel.SysLog{}).Count(&logTotal)

	var exceptionTotal int64
	db.DB.WithContext(ctx).Model(&logModel.SysLog{}).Where("category = ?", "EXCEPTION").Count(&exceptionTotal)

	var exceptionToday int64
	db.DB.WithContext(ctx).Model(&logModel.SysLog{}).
		Where("category = ?", "EXCEPTION").
		Where("DATE(op_time) = CURDATE()").
		Count(&exceptionToday)

	return &LogAnalysisData{
		LogTotal:       int(logTotal),
		LogException:   int(exceptionTotal),
		ExceptionToday: int(exceptionToday),
	}
}

func Dashboard(c *gin.Context) *DashboardVO {
	ctx := context.Background()
	stats := DashboardStats{}

	db.DB.WithContext(ctx).Table("sys_user").Count(&stats.TotalUsers)
	db.DB.WithContext(ctx).Table("sys_user").Where("status = ?", string(enums.UserStatusActive)).Count(&stats.ActiveUsers)
	db.DB.WithContext(ctx).Table("sys_role").Count(&stats.TotalRoles)
	db.DB.WithContext(ctx).Table("sys_org").Count(&stats.TotalOrgs)
	db.DB.WithContext(ctx).Table("sys_config").Count(&stats.TotalConfigs)

	clientStats := ClientStats{}
	db.DB.WithContext(ctx).Table("client_user").Count(&clientStats.TotalUsers)
	db.DB.WithContext(ctx).Table("client_user").Where("status = ?", string(enums.UserStatusActive)).Count(&clientStats.ActiveUsers)

	sysInfo := SysInfo{
		OsName:  runtime.GOOS,
		RunTime: time.Now().Format("2006-01-02 15:04:05"),
	}

	return &DashboardVO{
		Stats:       stats,
		ClientStats: clientStats,
		SysInfo:     sysInfo,
	}
}
