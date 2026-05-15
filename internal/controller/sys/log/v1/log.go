package v1

import (
	"context"
	"fmt"
	"net/url"

	"github.com/gogf/gf/v2/errors/gerror"
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/sys/log/v1"
	"hei-goframe/internal/service/auth"
	"hei-goframe/internal/service/sys/log"
	logService "hei-goframe/internal/service/sys/log"
	"hei-goframe/utility"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.LogPageReq) (res *api.LogPageRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:page"); err != nil {
		return nil, err
	}
	result, err := logService.Page(ctx, req.Keyword, req.Category, req.ExeStatus, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.LogPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.LogCreateReq) (res *api.LogCreateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:create"); err != nil {
		return nil, err
	}
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
	if err := auth.MustPerm(ctx, "sys:log:modify"); err != nil {
		return nil, err
	}
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
	if err := auth.MustPerm(ctx, "sys:log:remove"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "删除操作日志")()
	err = logService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.LogRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.LogDetailReq) (res *api.LogDetailRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:detail"); err != nil {
		return nil, err
	}
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
	if err := auth.MustPerm(ctx, "sys:log:remove"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "按分类清空日志")()
	err = logService.DeleteByCategory(ctx, req.Category)
	if err != nil {
		return nil, err
	}
	return &api.LogDeleteByCategoryRes{}, nil
}

func (c *ControllerV1) Export(ctx context.Context, req *api.LogExportReq) (res *api.LogExportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:export"); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导出操作日志数据")()
	buffer, err := logService.Export(ctx, req.ExportType, utility.SplitIds(req.SelectedId), req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("操作日志数据.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.LogExportRes{}, nil
}

func (c *ControllerV1) DownloadTemplate(ctx context.Context, req *api.LogTemplateReq) (res *api.LogTemplateRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:template"); err != nil {
		return nil, err
	}
	buffer, err := logService.DownloadTemplate(ctx)
	if err != nil {
		return nil, err
	}
	r := g.RequestFromCtx(ctx)
	filename := url.PathEscape("操作日志导入模板.xlsx")
	r.Response.Header().Set("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	r.Response.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename*=UTF-8''%s`, filename))
	r.Response.Write(buffer.Bytes())
	return &api.LogTemplateRes{}, nil
}

func (c *ControllerV1) Import(ctx context.Context, req *api.LogImportReq) (res *api.LogImportRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:import"); err != nil {
		return nil, err
	}
	if err := auth.CheckNoRepeatInline(ctx, 5000); err != nil {
		return nil, err
	}
	defer log.SysLog(ctx, "导入操作日志数据")()
	r := g.RequestFromCtx(ctx)
	file := r.GetUploadFile("file")
	if file == nil {
		return nil, gerror.New("请选择上传文件")
	}
	result, err := logService.Import(ctx, *file)
	if err != nil {
		return nil, err
	}
	res = &api.LogImportRes{}
	if err := gconv.Struct(result, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) VisLineChartData(ctx context.Context, req *api.LogVisLineChartReq) (res *api.LogVisLineChartRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:page"); err != nil {
		return nil, err
	}
	data, err := logService.VisLineChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogVisLineChartRes{}
	convertChartData(data, &res.Days, &res.Series)
	return res, nil
}

func (c *ControllerV1) VisPieChartData(ctx context.Context, req *api.LogVisPieChartReq) (res *api.LogVisPieChartRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:page"); err != nil {
		return nil, err
	}
	data, err := logService.VisPieChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogVisPieChartRes{}
	convertPieData(data, &res.Data)
	return res, nil
}

func (c *ControllerV1) OpBarChartData(ctx context.Context, req *api.LogOpBarChartReq) (res *api.LogOpBarChartRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:page"); err != nil {
		return nil, err
	}
	data, err := logService.OpBarChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogOpBarChartRes{}
	convertChartData(data, &res.Days, &res.Series)
	return res, nil
}

func (c *ControllerV1) OpPieChartData(ctx context.Context, req *api.LogOpPieChartReq) (res *api.LogOpPieChartRes, err error) {
	if err := auth.MustPerm(ctx, "sys:log:page"); err != nil {
		return nil, err
	}
	data, err := logService.OpPieChartData(ctx)
	if err != nil {
		return nil, err
	}
	res = &api.LogOpPieChartRes{}
	convertPieData(data, &res.Data)
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
