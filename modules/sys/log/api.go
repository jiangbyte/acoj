package log

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	syslog "hei-gin/core/log"
	"hei-gin/core/norepeat"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/log/page",
		auth.CheckPermission("sys:log:page"),
		PageHandler,
	)
	r.GET("/api/v1/sys/log/detail",
		auth.CheckPermission("sys:log:detail"),
		DetailHandler,
	)
	r.POST("/api/v1/sys/log/remove",
		syslog.SysLog("删除操作日志"),
		auth.CheckPermission("sys:log:remove"),
		RemoveHandler,
	)
	r.POST("/api/v1/sys/log/delete-by-category",
		syslog.SysLog("按分类清空日志"),
		auth.CheckPermission("sys:log:remove"),
		norepeat.NoRepeat(5000),
		DeleteByCategoryHandler,
	)
	r.POST("/api/v1/sys/log/create",
		auth.CheckPermission("sys:log:create"),
		CreateHandler,
	)
	r.POST("/api/v1/sys/log/modify",
		auth.CheckPermission("sys:log:modify"),
		ModifyHandler,
	)
	r.GET("/api/v1/sys/log/vis/line-chart-data",
		auth.CheckPermission("sys:log:page"),
		VisLineChartDataHandler,
	)
	r.GET("/api/v1/sys/log/vis/pie-chart-data",
		auth.CheckPermission("sys:log:page"),
		VisPieChartDataHandler,
	)
	r.GET("/api/v1/sys/log/op/bar-chart-data",
		auth.CheckPermission("sys:log:page"),
		OpBarChartDataHandler,
	)
	r.GET("/api/v1/sys/log/op/pie-chart-data",
		auth.CheckPermission("sys:log:page"),
		OpPieChartDataHandler,
	)
}

func PageHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.ValidationError(c, err)
		return
	}
	if p.Page <= 0 {
		p.Page = 1
	}
	if p.Size <= 0 {
		p.Size = 10
	}

	total, items, err := Page(p.Page, p.Size, p.Keyword, p.ExeStatus, p.ReqMethod)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []SysLogVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func DetailHandler(c *gin.Context) {
	var req DetailReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	item, err := Detail(req.ID)
	if err != nil {
		result.Failure(c, "未找到数据", 404)
		return
	}
	result.Success(c, toVO(item))
}

func RemoveHandler(c *gin.Context) {
	var req RemoveReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	if err := Remove(req.IDs); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func DeleteByCategoryHandler(c *gin.Context) {
	var req CleanReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := DeleteByCategory(req.Category); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func CreateHandler(c *gin.Context) {
	var req SysLogVO
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	item, err := Create(req)
	if err != nil {
		result.Failure(c, "创建失败", 500)
		return
	}
	result.Success(c, toVO(item))
}

func ModifyHandler(c *gin.Context) {
	var req SysLogVO
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := Modify(req); err != nil {
		result.Failure(c, "修改失败", 500)
		return
	}
	result.Success(c, nil)
}

func VisLineChartDataHandler(c *gin.Context) {
	data, err := VisLineChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

func VisPieChartDataHandler(c *gin.Context) {
	data, err := VisPieChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

func OpBarChartDataHandler(c *gin.Context) {
	data, err := OpBarChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}

func OpPieChartDataHandler(c *gin.Context) {
	data, err := OpPieChartData()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, data)
}
