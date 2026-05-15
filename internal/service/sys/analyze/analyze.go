package analyze

import (
	"context"
	"fmt"
	"runtime"
	"time"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
)

var serverStartTime = time.Now()

func Dashboard(ctx context.Context) (g.Map, error) {
	// Basic counts
	totalUsers, _ := dao.SysUser.Ctx().Ctx(ctx).Count()
	activeUsers, _ := dao.SysUser.Ctx().Ctx(ctx).Where("status", "ACTIVE").Count()
	totalRoles, _ := dao.SysRole.Ctx().Ctx(ctx).Count()
	totalOrgs, _ := dao.SysOrg.Ctx().Ctx(ctx).Count()
	totalConfigs, _ := dao.SysConfig.Ctx().Ctx(ctx).Count()
	totalNotices, _ := dao.SysNotice.Ctx().Ctx(ctx).Count()

	// User trend (monthly registration)
	userTrendRows, _ := dao.SysUser.Ctx().Ctx(ctx).
		Fields("DATE_FORMAT(created_at,'%Y-%m') as month", "COUNT(*) as count").
		Where("created_at IS NOT NULL").
		Group("month").
		OrderAsc("month").
		Limit(12).
		All()
	userTrend := make([]g.Map, 0)
	for _, row := range userTrendRows {
		userTrend = append(userTrend, g.Map{
			"month": row["month"].String(),
			"count": row["count"].Int(),
		})
	}

	// Client stats
	totalClientUsers, _ := dao.ClientUser.Ctx().Ctx(ctx).Count()
	activeClientUsers, _ := dao.ClientUser.Ctx().Ctx(ctx).Where("status", "ACTIVE").Count()

	// Client user trend (monthly registration)
	clientTrendRows, _ := dao.ClientUser.Ctx().Ctx(ctx).
		Fields("DATE_FORMAT(created_at,'%Y-%m') as month", "COUNT(*) as count").
		Where("created_at IS NOT NULL").
		Group("month").
		OrderAsc("month").
		Limit(12).
		All()
	clientTrend := make([]g.Map, 0)
	for _, row := range clientTrendRows {
		clientTrend = append(clientTrend, g.Map{
			"month": row["month"].String(),
			"count": row["count"].Int(),
		})
	}

	// Org user distribution
	orgDistRows, _ := dao.SysOrg.Ctx().Ctx(ctx).As("o").
		LeftJoin(dao.SysUser.Table+" u", "u.org_id = o.id").
		Fields("o.name", "COUNT(u.id) as count").
		Group("o.id").
		OrderDesc("count").
		All()
	orgDist := make([]g.Map, 0)
	for _, row := range orgDistRows {
		orgDist = append(orgDist, g.Map{
			"name":  row["name"].String(),
			"count": row["count"].Int(),
		})
	}

	// Role category distribution
	roleCatRows, _ := dao.SysRole.Ctx().Ctx(ctx).
		Fields("category", "COUNT(*) as count").
		Group("category").
		OrderDesc("count").
		All()
	roleCatDist := make([]g.Map, 0)
	for _, row := range roleCatRows {
		roleCatDist = append(roleCatDist, g.Map{
			"category": row["category"].String(),
			"count":    row["count"].Int(),
		})
	}

	// System info
	serverIp, _ := g.Cfg().Get(ctx, "hei.server.ip")
	ipStr := serverIp.String()
	if ipStr == "" {
		ipStr = "unknown"
	}

	uptime := time.Since(serverStartTime)
	days := int(uptime.Hours()) / 24
	hours := int(uptime.Hours()) % 24
	minutes := int(uptime.Minutes()) % 60
	var runTime string
	if days > 0 {
		runTime = fmt.Sprintf("%d天 %d小时 %d分钟", days, hours, minutes)
	} else {
		runTime = fmt.Sprintf("%d小时 %d分钟", hours, minutes)
	}

	sysInfo := g.Map{
		"go_version": runtime.Version(),
		"os_name":    runtime.GOOS,
		"server_ip":  ipStr,
		"run_time":   runTime,
	}

	return g.Map{
		"stats": g.Map{
			"total_users":   totalUsers,
			"active_users":  activeUsers,
			"total_roles":   totalRoles,
			"total_orgs":    totalOrgs,
			"total_configs": totalConfigs,
			"total_notices": totalNotices,
		},
		"user_trend": userTrend,
		"client_stats": g.Map{
			"total_users":  totalClientUsers,
			"active_users": activeClientUsers,
		},
		"client_trend":               clientTrend,
		"org_user_distribution":      orgDist,
		"role_category_distribution": roleCatDist,
		"sys_info":                   sysInfo,
	}, nil
}
