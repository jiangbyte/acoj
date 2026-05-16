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

	data.Users = toInt64(db.Client.SysUser.Query().Count(ctx))
	data.Roles = toInt64(db.Client.SysRole.Query().Count(ctx))
	data.Orgs = toInt64(db.Client.SysOrg.Query().Count(ctx))
	data.Groups = toInt64(db.Client.SysGroup.Query().Count(ctx))
	data.Positions = toInt64(db.Client.SysPosition.Query().Count(ctx))
	data.Banners = toInt64(db.Client.SysBanner.Query().Count(ctx))
	data.Files = toInt64(db.Client.SysFile.Query().Count(ctx))
	data.Notices = toInt64(db.Client.SysNotice.Query().Count(ctx))
	data.Dicts = toInt64(db.Client.SysDict.Query().Count(ctx))

	return data, err
}

func toInt64(v int, err error) int64 {
	if err != nil {
		return 0
	}
	return int64(v)
}
