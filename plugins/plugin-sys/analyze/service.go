package analyze

import (
	"context"
	"fmt"
	"log"
	"net"
	"strings"
	"runtime"
	"time"

	"github.com/gin-gonic/gin"

	"hei-gin/sdk/enums"
	"hei-gin/sdk/db"
	logModel "hei-gin/plugins/plugin-sys/log"
)

// Server start time, used for computing uptime/runtime display
var serverStartTime = time.Now()

// getServerIP attempts to detect a non-loopback IPv4 address
func getServerIP() string {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return ""
	}
	for _, addr := range addrs {
		if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() && ipnet.IP.To4() != nil {
			ip := ipnet.IP.String()
			if ip != "0.0.0.0" && !strings.HasPrefix(ip, "169.254") {
				return ip
			}
		}
	}
	return ""
}

func formatDuration(d time.Duration) string {
	days := int(d.Hours()) / 24
	hours := int(d.Hours()) % 24
	mins := int(d.Minutes()) % 60
	if days > 0 {
		return fmt.Sprintf("%d天%d小时%d分钟", days, hours, mins)
	}
	if hours > 0 {
		return fmt.Sprintf("%d小时%d分钟", hours, mins)
	}
	return fmt.Sprintf("%d分钟", mins)
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
	db.DB.WithContext(ctx).Table("sys_notice").Count(&stats.TotalNotices)

	clientStats := ClientStats{}
	db.DB.WithContext(ctx).Table("client_user").Count(&clientStats.TotalUsers)
	db.DB.WithContext(ctx).Table("client_user").Where("status = ?", string(enums.UserStatusActive)).Count(&clientStats.ActiveUsers)

	// User growth trend: monthly registration counts over the last 12 months
	userTrend := getMonthlyTrend(ctx, "sys_user")
	clientTrend := getMonthlyTrend(ctx, "client_user")

	// Org user distribution
	orgDist := getOrgUserDistribution(ctx)

	// Role category distribution
	roleDist := getRoleCategoryDistribution(ctx)

	sysInfo := SysInfo{
		OsName:   runtime.GOOS,
		ServerIP: getServerIP(),
		RunTime:  fmt.Sprintf("已运行 %s", formatDuration(time.Since(serverStartTime))),
	}

	return &DashboardVO{
		Stats:                    stats,
		ClientStats:              clientStats,
		UserTrend:                userTrend,
		ClientTrend:              clientTrend,
		OrgUserDistribution:      orgDist,
		RoleCategoryDistribution: roleDist,
		SysInfo:                  sysInfo,
	}
}

func getMonthlyTrend(ctx context.Context, table string) []TrendItem {
	type MonthlyCount struct {
		Month string
		Count int
	}
	var rows []MonthlyCount
	db.DB.WithContext(ctx).Table(table).
		Select("DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count").
		Where("created_at IS NOT NULL").
		Group("month").
		Order("month ASC").
		Limit(12).
		Find(&rows)
	result := make([]TrendItem, len(rows))
	for i, r := range rows {
		result[i] = TrendItem{Month: r.Month, Count: r.Count}
	}
	if result == nil {
		result = []TrendItem{}
	}
	return result
}

func getOrgUserDistribution(ctx context.Context) []OrgUserDistribution {
	type OrgCount struct {
		OrgID string
		Count int
	}
	var rows []OrgCount
	db.DB.WithContext(ctx).Table("sys_user").
		Select("org_id, COUNT(*) AS count").
		Where("org_id IS NOT NULL AND org_id != ''").
		Group("org_id").
		Find(&rows)
	orgIDs := make([]string, len(rows))
	for i, r := range rows {
		orgIDs[i] = r.OrgID
	}
	orgNames := make(map[string]string)
	if len(orgIDs) > 0 {
		type orgNameRow struct{ ID, Name string }
		var orgRows []orgNameRow
		db.DB.WithContext(ctx).Table("sys_org").Select("id, name").Where("id IN ?", orgIDs).Find(&orgRows)
		for _, o := range orgRows {
			orgNames[o.ID] = o.Name
		}
	}
	result := make([]OrgUserDistribution, 0, len(rows))
	for _, r := range rows {
		name := orgNames[r.OrgID]
		if name == "" {
			name = "未分配"
		}
		result = append(result, OrgUserDistribution{Name: name, Count: r.Count})
	}
	if result == nil {
		result = []OrgUserDistribution{}
	}
	return result
}

func getRoleCategoryDistribution(ctx context.Context) []CategoryDistribution {
	type CatCount struct {
		Category string
		Count    int
	}
	var rows []CatCount
	db.DB.WithContext(ctx).Table("sys_role").
		Select("category, COUNT(*) AS count").
		Group("category").
		Find(&rows)
	result := make([]CategoryDistribution, len(rows))
	for i, r := range rows {
		result[i] = CategoryDistribution{Category: r.Category, Count: r.Count}
	}
	if result == nil {
		result = []CategoryDistribution{}
	}
	return result
}
