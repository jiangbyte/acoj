package analyze

import (
	"context"
	"runtime"

	"github.com/gogf/gf/v2/frame/g"

	"hei-goframe/internal/dao"
)

func Dashboard(ctx context.Context) (g.Map, error) {
	// Basic counts
	totalUsers, _ := dao.SysUser.Ctx().Ctx(ctx).Count()
	activeUsers, _ := dao.SysUser.Ctx().Ctx(ctx).Where("status", "ACTIVE").Count()
	totalRoles, _ := dao.SysRole.Ctx().Ctx(ctx).Count()
	totalOrgs, _ := dao.SysOrg.Ctx().Ctx(ctx).Count()
	totalConfigs, _ := dao.SysConfig.Ctx().Ctx(ctx).Count()
	totalNotices, _ := dao.SysNotice.Ctx().Ctx(ctx).Count()

	// User trend (monthly registration)
	userTrend, _ := dao.SysUser.Ctx().Ctx(ctx).
		Fields("DATE_FORMAT(created_at,'%Y-%m') as month", "COUNT(*) as count").
		Group("month").
		OrderAsc("month").
		All()

	trendList := make([]g.Map, 0)
	for _, row := range userTrend {
		trendList = append(trendList, g.Map{
			"month": row["month"].String(),
			"count": row["count"].Int(),
		})
	}

	// Client stats
	totalClients, _ := dao.ClientUser.Ctx().Ctx(ctx).Count()
	clientStats := make([]g.Map, 0)
	clientStats = append(clientStats, g.Map{
		"client": "总用户",
		"count":  totalClients,
	})

	// System info
	hostname, _ := g.Cfg().Get(ctx, "hei.server.hostname")
	serverIp, _ := g.Cfg().Get(ctx, "hei.server.ip")
	osName := runtime.GOOS

	sysInfo := g.Map{
		"os": osName,
		"ip": serverIp.String(),
	}
	if hostname.String() != "" {
		sysInfo["hostname"] = hostname.String()
	}

	return g.Map{
		"total_users":   totalUsers,
		"active_users":  activeUsers,
		"total_roles":   totalRoles,
		"total_orgs":    totalOrgs,
		"total_configs": totalConfigs,
		"total_notices": totalNotices,
		"user_trend":    trendList,
		"client_stats":  clientStats,
		"sys_info":      sysInfo,
	}, nil
}
