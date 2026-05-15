package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	api "hei-goframe/api/sys/session/v1"
	"hei-goframe/internal/service/auth"
	sessionService "hei-goframe/internal/service/sys/session"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Analysis(ctx context.Context, req *api.SessionAnalysisReq) (res *api.SessionAnalysisRes, err error) {
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
	result, err := sessionService.Page(ctx, req.Keyword, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.SessionPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Exit(ctx context.Context, req *api.SessionExitReq) (res *api.SessionExitRes, err error) {
	err = sessionService.Exit(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	return &api.SessionExitRes{}, nil
}

func (c *ControllerV1) Tokens(ctx context.Context, req *api.SessionTokensReq) (res *api.SessionTokensRes, err error) {
	tokens, err := sessionService.Tokens(ctx, req.UserId)
	if err != nil {
		return nil, err
	}
	if tokens == nil {
		tokens = make([]string, 0)
	}
	return &api.SessionTokensRes{Tokens: tokens}, nil
}

func (c *ControllerV1) ExitToken(ctx context.Context, req *api.SessionExitTokenReq) (res *api.SessionExitTokenRes, err error) {
	err = sessionService.ExitToken(ctx, req.UserId, req.Token)
	if err != nil {
		return nil, err
	}
	return &api.SessionExitTokenRes{}, nil
}

func (c *ControllerV1) ChartData(ctx context.Context, req *api.SessionChartDataReq) (res *api.SessionChartDataRes, err error) {
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
	tokenStr := r.Header.Get(auth.BusinessAuth.GetTokenName())
	id, _ := auth.BusinessAuth.GetLoginId(ctx, tokenStr)
	return id
}

func convertSessionChartData(data g.Map, res *api.SessionChartDataRes) {
	if v, ok := data["days"]; ok {
		if d, ok := v.([]string); ok {
			res.Days = d
		}
	}
	if res.Days == nil {
		res.Days = make([]string, 0)
	}
	if v, ok := data["series"]; ok {
		if s, ok := v.([]g.Map); ok {
			for _, item := range s {
				name, _ := item["name"].(string)
				var dataArr []int
				if d, ok := item["data"].([]int); ok {
					dataArr = d
				}
				res.Series = append(res.Series, api.SessionChartSeriesItem{Name: name, Data: dataArr})
			}
		}
	}
	if res.Series == nil {
		res.Series = make([]api.SessionChartSeriesItem, 0)
	}
	if v, ok := data["list"]; ok {
		if items, ok := v.([]g.Map); ok {
			for _, item := range items {
				category, _ := item["category"].(string)
				total, _ := item["total"].(int)
				res.List = append(res.List, api.SessionChartPieItem{Category: category, Total: total})
			}
		}
	}
	if res.List == nil {
		res.List = make([]api.SessionChartPieItem, 0)
	}
}
