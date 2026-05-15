package analyze

import (
	"context"

	"hei-gin/core/db"
)

type DashboardData struct {
	Users     int64 `json:"users"`
	Roles     int64 `json:"roles"`
	Orgs      int64 `json:"orgs"`
	Groups    int64 `json:"groups"`
	Positions int64 `json:"positions"`
	Banners   int64 `json:"banners"`
	Files     int64 `json:"files"`
	Notices   int64 `json:"notices"`
	Dicts     int64 `json:"dicts"`
}

func Dashboard() (*DashboardData, error) {
	ctx := context.Background()

	data := &DashboardData{}
	var err error

	data.Users, _ = countTable(ctx, "sys_user")
	data.Roles, _ = countTable(ctx, "sys_role")
	data.Orgs, _ = countTable(ctx, "sys_org")
	data.Groups, _ = countTable(ctx, "sys_group")
	data.Positions, _ = countTable(ctx, "sys_position")
	data.Banners, _ = countTable(ctx, "sys_banner")
	data.Files, _ = countTable(ctx, "sys_file")
	data.Notices, _ = countTable(ctx, "sys_notice")
	data.Dicts, _ = countTable(ctx, "sys_dict")

	return data, err
}

func countTable(ctx context.Context, tableName string) (int64, error) {
	var count int64
	row := db.RawDB.QueryRowContext(ctx, "SELECT COUNT(*) FROM "+tableName)
	if err := row.Scan(&count); err != nil {
		return 0, err
	}
	return count, nil
}
