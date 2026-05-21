package analyze

import (
	"context"
	"fmt"
	"log"
	"net"
	"runtime"
	"sort"
	"time"

	"hei-gin/core/db"
	ent "hei-gin/ent/gen"
	"hei-gin/ent/gen/clientuser"
	"hei-gin/ent/gen/sysuser"

	"github.com/gin-gonic/gin"
)

var ServerStartTime = time.Now()

func Dashboard(c *gin.Context) *DashboardVO {
	ctx := context.Background()

	totalUsers, err := db.Client.SysUser.Query().Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query total users: %v", err)
	}
	activeUsers, err := db.Client.SysUser.Query().Where(sysuser.StatusEQ("ACTIVE")).Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query active users: %v", err)
	}
	totalRoles, err := db.Client.SysRole.Query().Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query total roles: %v", err)
	}
	totalOrgs, err := db.Client.SysOrg.Query().Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query total orgs: %v", err)
	}
	totalConfigs, err := db.Client.SysConfig.Query().Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query total configs: %v", err)
	}
	totalNotices, err := db.Client.SysNotice.Query().Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query total notices: %v", err)
	}

	stats := DashboardStats{
		TotalUsers:   totalUsers,
		ActiveUsers:  activeUsers,
		TotalRoles:   totalRoles,
		TotalOrgs:    totalOrgs,
		TotalConfigs: totalConfigs,
		TotalNotices: totalNotices,
	}

	clientTotal, err := db.Client.ClientUser.Query().Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query client total: %v", err)
	}
	clientActive, err := db.Client.ClientUser.Query().Where(clientuser.StatusEQ("ACTIVE")).Count(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query client active: %v", err)
	}
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
	now := time.Now()
	result := make([]TrendItem, 12)
	for i := 11; i >= 0; i-- {
		month := now.AddDate(0, -i, 0).Format("2006-01")
		result[11-i] = TrendItem{Month: month, Count: 0}
	}

	// Per-month COUNT queries are efficient with index on created_at
	for i, item := range result {
		start, err := time.Parse("2006-01", item.Month)
		if err != nil {
			continue
		}
		end := start.AddDate(0, 1, 0)
		count, err := db.Client.SysUser.Query().
			Where(sysuser.CreatedAtGTE(start), sysuser.CreatedAtLT(end)).
			Count(ctx)
		if err != nil {
			log.Printf("[ANALYZE] failed to query user trend for %s: %v", item.Month, err)
		}
		result[i].Count = count
	}
	return result
}

func getClientUserTrend(ctx context.Context) []TrendItem {
	now := time.Now()
	result := make([]TrendItem, 12)
	for i := 11; i >= 0; i-- {
		month := now.AddDate(0, -i, 0).Format("2006-01")
		result[11-i] = TrendItem{Month: month, Count: 0}
	}

	// Per-month COUNT queries are efficient with index on created_at
	for i, item := range result {
		start, err := time.Parse("2006-01", item.Month)
		if err != nil {
			continue
		}
		end := start.AddDate(0, 1, 0)
		count, err := db.Client.ClientUser.Query().
			Where(clientuser.CreatedAtGTE(start), clientuser.CreatedAtLT(end)).
			Count(ctx)
		if err != nil {
			log.Printf("[ANALYZE] failed to query client user trend for %s: %v", item.Month, err)
		}
		result[i].Count = count
	}
	return result
}

func getOrgUserDistribution(ctx context.Context) []OrgUserDistribution {
	orgs, err := db.Client.SysOrg.Query().All(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query org user distribution: %v", err)
		return nil
	}

	// Single GROUP BY aggregation query instead of N+1
	var aggResults []struct {
		OrgID string `json:"org_id"`
		Count int    `json:"count"`
	}
	err = db.Client.SysUser.Query().
		GroupBy(sysuser.FieldOrgID).
		Aggregate(ent.As(ent.Count(), "count")).
		Scan(ctx, &aggResults)
	if err != nil {
		log.Printf("[ANALYZE] failed to aggregate org user counts: %v", err)
		// Fall back to individual queries
		result := make([]OrgUserDistribution, 0, len(orgs))
		for _, o := range orgs {
			count, err := db.Client.SysUser.Query().Where(sysuser.OrgID(o.ID)).Count(ctx)
			if err != nil {
				log.Printf("[ANALYZE] failed to query user count for org %s: %v", o.ID, err)
			}
			result = append(result, OrgUserDistribution{Name: o.Name, Count: count})
		}
		sort.Slice(result, func(i, j int) bool {
			return result[i].Count > result[j].Count
		})
		return result
	}

	orgCountMap := make(map[string]int, len(aggResults))
	for _, r := range aggResults {
		orgCountMap[r.OrgID] = r.Count
	}

	result := make([]OrgUserDistribution, 0, len(orgs))
	for _, o := range orgs {
		result = append(result, OrgUserDistribution{Name: o.Name, Count: orgCountMap[o.ID]})
	}
	sort.Slice(result, func(i, j int) bool {
		return result[i].Count > result[j].Count
	})
	return result
}

func getRoleCategoryDistribution(ctx context.Context) []CategoryDistribution {
	roles, err := db.Client.SysRole.Query().All(ctx)
	if err != nil {
		log.Printf("[ANALYZE] failed to query role category distribution: %v", err)
		return nil
	}
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
