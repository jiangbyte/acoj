package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/client/session/v1"
	"hei-goframe/internal/service/auth"
	sessionService "hei-goframe/internal/service/client/session"
	"hei-goframe/internal/service/sys/log"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Analysis(ctx context.Context, req *api.SessionAnalysisReq) (res *api.SessionAnalysisRes, err error) {
	if err := auth.MustPerm(ctx, "sys:session:page"); err != nil {
		return nil, err
	}
	data, err := sessionService.Analysis(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.SessionAnalysisRes{
		TotalCount:        data["total_count"].(int),
		MaxTokenCount:     data["max_token_count"].(int),
		OneHourNewlyAdded: data["one_hour_newly_added"].(int),
		ProportionBC:      data["proportion_of_b_and_c"].(string),
	}
	return res, nil
}

func (c *ControllerV1) Page(ctx context.Context, req *api.SessionPageReq) (res *api.SessionPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:session:page"); err != nil {
		return nil, err
	}
	result, err := sessionService.Page(ctx, req.Keyword, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.SessionPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Exit(ctx context.Context, req *api.SessionExitReq) (res *api.SessionExitRes, err error) {
	if err := auth.MustPerm(ctx, "sys:session:exit"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "强退C端用户会话")()
	err = sessionService.Exit(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	return &api.SessionExitRes{}, nil
}

func (c *ControllerV1) Tokens(ctx context.Context, req *api.SessionTokensReq) (res *api.SessionTokensRes, err error) {
	if err := auth.MustPerm(ctx, "sys:session:page"); err != nil {
		return nil, err
	}
	data, err := sessionService.Tokens(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	items := make([]api.SessionTokenItem, 0)
	for _, item := range data {
		items = append(items, api.SessionTokenItem{
			Token:          gconv.String(item["token"]),
			CreatedAt:      gconv.String(item["created_at"]),
			Timeout:        gconv.String(item["timeout"]),
			TimeoutSeconds: gconv.Int(item["timeout_seconds"]),
			DeviceType:     gconv.String(item["device_type"]),
			DeviceId:       gconv.String(item["device_id"]),
		})
	}
	result := api.SessionTokensRes(items)
	return &result, nil
}

func (c *ControllerV1) ExitToken(ctx context.Context, req *api.SessionExitTokenReq) (res *api.SessionExitTokenRes, err error) {
	if err := auth.MustPerm(ctx, "sys:session:exit"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "强退C端指定令牌")()
	err = sessionService.ExitToken(ctx, req.UserId, req.Token)
	if err != nil {
		return nil, err
	}
	return &api.SessionExitTokenRes{}, nil
}

func (c *ControllerV1) ChartData(ctx context.Context, req *api.SessionChartDataReq) (res *api.SessionChartDataRes, err error) {
	if err := auth.MustPerm(ctx, "sys:session:page"); err != nil {
		return nil, err
	}
	data, err := sessionService.ChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.SessionChartDataRes{}
	convertSessionChartData(data, res)
	return res, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return ""
	}
	tokenStr := r.Header.Get(auth.ConsumerAuth.GetTokenName())
	id, _ := auth.ConsumerAuth.GetLoginId(ctx, tokenStr)
	return id
}

func convertSessionChartData(data g.Map, res *api.SessionChartDataRes) {
	// Bar chart
	if barChart, ok := data["bar_chart"].(g.Map); ok {
		barData := &api.BarChartData{}
		if days, ok := barChart["days"].([]string); ok {
			barData.Days = days
		}
		if barData.Days == nil {
			barData.Days = make([]string, 0)
		}
		if series, ok := barChart["series"].([]g.Map); ok {
			for _, item := range series {
				name, _ := item["name"].(string)
				var dataArr []int
				if d, ok := item["data"].([]int); ok {
					dataArr = d
				}
				barData.Series = append(barData.Series, api.SessionChartSeriesItem{Name: name, Data: dataArr})
			}
		}
		if barData.Series == nil {
			barData.Series = make([]api.SessionChartSeriesItem, 0)
		}
		res.BarChart = barData
	}

	// Pie chart
	if pieChart, ok := data["pie_chart"].(g.Map); ok {
		pieData := &api.PieChartData{}
		if items, ok := pieChart["data"].([]g.Map); ok {
			for _, item := range items {
				category, _ := item["category"].(string)
				total, _ := item["total"].(int)
				pieData.Data = append(pieData.Data, api.SessionChartPieItem{Category: category, Total: total})
			}
		}
		if pieData.Data == nil {
			pieData.Data = make([]api.SessionChartPieItem, 0)
		}
		res.PieChart = pieData
	}
}
