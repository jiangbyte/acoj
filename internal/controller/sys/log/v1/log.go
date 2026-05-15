package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"

	api "hei-goframe/api/sys/log/v1"
	logService "hei-goframe/internal/service/sys/log"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.LogPageReq) (res *api.LogPageRes, err error) {
	result, err := logService.Page(ctx, req.Keyword, req.Category, req.ExeStatus, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.LogPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.LogCreateReq) (res *api.LogCreateRes, err error) {
	err = logService.Create(ctx, req.Category, req.Name, req.ExeStatus, req.ExeMessage,
		req.OpIp, req.OpAddress, req.OpBrowser, req.OpOs,
		req.ClassName, req.MethodName, req.ReqMethod, req.ReqUrl,
		req.ParamJson, req.ResultJson, req.OpTime, req.TraceId, req.OpUser, req.SignData)
	if err != nil {
		return nil, err
	}
	return &api.LogCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.LogModifyReq) (res *api.LogModifyRes, err error) {
	err = logService.Modify(ctx, req.Id, req.Category, req.Name, req.ExeStatus, req.ExeMessage,
		req.OpIp, req.OpAddress, req.OpBrowser, req.OpOs,
		req.ClassName, req.MethodName, req.ReqMethod, req.ReqUrl,
		req.ParamJson, req.ResultJson, req.OpTime, req.TraceId, req.OpUser, req.SignData)
	if err != nil {
		return nil, err
	}
	return &api.LogModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.LogRemoveReq) (res *api.LogRemoveRes, err error) {
	err = logService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.LogRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.LogDetailReq) (res *api.LogDetailRes, err error) {
	data, err := logService.Detail(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	if data == nil {
		return nil, nil
	}
	res = &api.LogDetailRes{
		Id:         data["id"].(string),
		Category:   data["category"].(string),
		Name:       data["name"].(string),
		ExeStatus:  data["exe_status"].(string),
		ExeMessage: data["exe_message"].(string),
		OpIp:       data["op_ip"].(string),
		OpAddress:  data["op_address"].(string),
		OpBrowser:  data["op_browser"].(string),
		OpOs:       data["op_os"].(string),
		ClassName:  data["class_name"].(string),
		MethodName: data["method_name"].(string),
		ReqMethod:  data["req_method"].(string),
		ReqUrl:     data["req_url"].(string),
		ParamJson:  data["param_json"].(string),
		ResultJson: data["result_json"].(string),
		OpTime:     data["op_time"].(string),
		TraceId:    data["trace_id"].(string),
		OpUser:     data["op_user"].(string),
		SignData:   data["sign_data"].(string),
		CreatedAt:  data["created_at"].(string),
		CreatedBy:  data["created_by"].(string),
		UpdatedAt:  data["updated_at"].(string),
		UpdatedBy:  data["updated_by"].(string),
	}
	return res, nil
}

func (c *ControllerV1) DeleteByCategory(ctx context.Context, req *api.LogDeleteByCategoryReq) (res *api.LogDeleteByCategoryRes, err error) {
	err = logService.DeleteByCategory(ctx, req.Category)
	if err != nil {
		return nil, err
	}
	return &api.LogDeleteByCategoryRes{}, nil
}

func (c *ControllerV1) VisLineChartData(ctx context.Context, req *api.LogVisLineChartReq) (res *api.LogVisLineChartRes, err error) {
	data, err := logService.VisLineChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogVisLineChartRes{}
	convertChartData(data, &res.Days, &res.Series)
	return res, nil
}

func (c *ControllerV1) VisPieChartData(ctx context.Context, req *api.LogVisPieChartReq) (res *api.LogVisPieChartRes, err error) {
	data, err := logService.VisPieChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogVisPieChartRes{}
	convertPieData(data, &res.List)
	return res, nil
}

func (c *ControllerV1) OpBarChartData(ctx context.Context, req *api.LogOpBarChartReq) (res *api.LogOpBarChartRes, err error) {
	data, err := logService.OpBarChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogOpBarChartRes{}
	convertChartData(data, &res.Days, &res.Series)
	return res, nil
}

func (c *ControllerV1) OpPieChartData(ctx context.Context, req *api.LogOpPieChartReq) (res *api.LogOpPieChartRes, err error) {
	data, err := logService.OpPieChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogOpPieChartRes{}
	convertPieData(data, &res.List)
	return res, nil
}

// --- Internal helpers ---

func convertChartData(data g.Map, days *[]string, series *[]api.SeriesItem) {
	if v, ok := data["days"]; ok {
		if d, ok := v.([]string); ok {
			*days = d
		}
	}
	if *days == nil {
		*days = make([]string, 0)
	}
	if v, ok := data["series"]; ok {
		if s, ok := v.([]g.Map); ok {
			for _, item := range s {
				name, _ := item["name"].(string)
				var dataArr []int
				if d, ok := item["data"].([]int); ok {
					dataArr = d
				}
				*series = append(*series, api.SeriesItem{Name: name, Data: dataArr})
			}
		}
	}
	if *series == nil {
		*series = make([]api.SeriesItem, 0)
	}
}

func convertPieData(data g.Map, list *[]api.PieChartItem) {
	if v, ok := data["list"]; ok {
		if items, ok := v.([]g.Map); ok {
			for _, item := range items {
				category, _ := item["category"].(string)
				total, _ := item["total"].(int)
				*list = append(*list, api.PieChartItem{Category: category, Total: total})
			}
		}
	}
	if *list == nil {
		*list = make([]api.PieChartItem, 0)
	}
}
