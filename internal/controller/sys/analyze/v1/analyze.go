package v1

import (
	"context"

	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/analyze/v1"
	analyzeService "hei-goframe/internal/service/sys/analyze"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Dashboard(ctx context.Context, req *api.DashboardReq) (res *api.DashboardRes, err error) {
	data, err := analyzeService.Dashboard(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.DashboardRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}
