package analyze

import (
	"context"
	"fmt"
	"net"
	"runtime"
	"sort"
	"time"

	"hei-gin/core/db"
	"hei-gin/ent/gen/clientuser"
	"hei-gin/ent/gen/sysuser"

	"github.com/gin-gonic/gin"
)

var ServerStartTime = time.Now()

func Dashboard(c *gin.Context) *DashboardVO {
	ctx := context.Background()

	totalUsers, _ := db.Client.SysUser.Query().Count(ctx)
	activeUsers, _ := db.Client.SysUser.Query().Where(sysuser.StatusEQ("ACTIVE")).Count(ctx)
	totalRoles, _ := db.Client.SysRole.Query().Count(ctx)
	totalOrgs, _ := db.Client.SysOrg.Query().Count(ctx)
	totalConfigs, _ := db.Client.SysConfig.Query().Count(ctx)
	totalNotices, _ := db.Client.SysNotice.Query().Count(ctx)

	stats := DashboardStats{
		TotalUsers:   totalUsers,
		ActiveUsers:  activeUsers,
		TotalRoles:   totalRoles,
		TotalOrgs:    totalOrgs,
		TotalConfigs: totalConfigs,
		TotalNotices: totalNotices,
	}

	clientTotal, _ := db.Client.ClientUser.Query().Count(ctx)
	clientActive, _ := db.Client.ClientUser.Query().Where(clientuser.StatusEQ("ACTIVE")).Count(ctx)
	clientStats := ClientStats{
		TotalUsers:  clientTotal,
		ActiveUsers: clientActive,
	}

	userTrend := getUserTrend(ctx)
	clientTrend := getClientUserTrend(ctx)
	orgDistribution := getOrgUserDistribution(ctx)
	roleDistribution := getRoleCategoryDistribution(ctx)
	sysInfo := getSysInfo()

	return &DashboardVO{
		Stats:                    stats,
		ClientStats:              clientStats,
		UserTrend:                userTrend,
		ClientTrend:              clientTrend,
		OrgUserDistribution:      orgDistribution,
		RoleCategoryDistribution: roleDistribution,
		SysInfo:                  sysInfo,
	}
}

func getUserTrend(ctx context.Context) []TrendItem {
	users, _ := db.Client.SysUser.Query().All(ctx)
	monthMap := make(map[string]int)
	for _, u := range users {
		if u.CreatedAt != nil {
			month := u.CreatedAt.Format("2006-01")
			monthMap[month]++
		}
	}
	result := make([]TrendItem, 0)
	now := time.Now()
	for i := 11; i >= 0; i-- {
		month := now.AddDate(0, -i, 0).Format("2006-01")
		result = append(result, TrendItem{Month: month, Count: monthMap[month]})
	}
	return result
}

func getClientUserTrend(ctx context.Context) []TrendItem {
	users, _ := db.Client.ClientUser.Query().All(ctx)
	monthMap := make(map[string]int)
	for _, u := range users {
		if u.CreatedAt != nil {
			month := u.CreatedAt.Format("2006-01")
			monthMap[month]++
		}
	}
	result := make([]TrendItem, 0)
	now := time.Now()
	for i := 11; i >= 0; i-- {
		month := now.AddDate(0, -i, 0).Format("2006-01")
		result = append(result, TrendItem{Month: month, Count: monthMap[month]})
	}
	return result
}

func getOrgUserDistribution(ctx context.Context) []OrgUserDistribution {
	orgs, _ := db.Client.SysOrg.Query().All(ctx)
	result := make([]OrgUserDistribution, 0)
	for _, o := range orgs {
		count, _ := db.Client.SysUser.Query().Where(sysuser.OrgID(o.ID)).Count(ctx)
		result = append(result, OrgUserDistribution{Name: o.Name, Count: count})
	}
	sort.Slice(result, func(i, j int) bool {
		return result[i].Count > result[j].Count
	})
	return result
}

func getRoleCategoryDistribution(ctx context.Context) []CategoryDistribution {
	roles, _ := db.Client.SysRole.Query().All(ctx)
	catMap := make(map[string]int)
	for _, r := range roles {
		catMap[r.Category]++
	}
	result := make([]CategoryDistribution, 0)
	for cat, count := range catMap {
		result = append(result, CategoryDistribution{Category: cat, Count: count})
	}
	sort.Slice(result, func(i, j int) bool {
		return result[i].Count > result[j].Count
	})
	return result
}

func getSysInfo() SysInfo {
	return SysInfo{
		OsName:   runtime.GOOS,
		ServerIP: getLocalIP(),
		RunTime:  formatDuration(time.Since(ServerStartTime)),
	}
}

func getLocalIP() string {
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		return "unknown"
	}
	for _, addr := range addrs {
		if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() && ipnet.IP.To4() != nil {
			return ipnet.IP.String()
		}
	}
	return "unknown"
}

func formatDuration(d time.Duration) string {
	days := int(d.Hours()) / 24
	hours := int(d.Hours()) % 24
	minutes := int(d.Minutes()) % 60
	if days > 0 {
		return fmt.Sprintf("%d天 %d小时 %d分钟", days, hours, minutes)
	}
	return fmt.Sprintf("%d小时 %d分钟", hours, minutes)
}
