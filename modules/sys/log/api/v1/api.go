package v1

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	middleware "hei-gin/core/auth/middleware"
	sysLog "hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	logPackage "hei-gin/modules/sys/log"
)

// RegisterRoutes registers all sys/log routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// GET /api/v1/sys/log/page
	r.GET("/api/v1/sys/log/page",
		middleware.HeiCheckPermission([]string{"sys:log:page"}),
		logPage,
	)

	// POST /api/v1/sys/log/create
	r.POST("/api/v1/sys/log/create",
		middleware.HeiCheckPermission([]string{"sys:log:create"}),
		logCreate,
	)

	// POST /api/v1/sys/log/modify
	r.POST("/api/v1/sys/log/modify",
		middleware.HeiCheckPermission([]string{"sys:log:modify"}),
		logModify,
	)

	// POST /api/v1/sys/log/remove
	r.POST("/api/v1/sys/log/remove",
		middleware.HeiCheckPermission([]string{"sys:log:remove"}),
		sysLog.SysLog("删除操作日志"),
		logRemove,
	)

	// GET /api/v1/sys/log/detail
	r.GET("/api/v1/sys/log/detail",
		middleware.HeiCheckPermission([]string{"sys:log:detail"}),
		logDetail,
	)

	// POST /api/v1/sys/log/delete-by-category
	r.POST("/api/v1/sys/log/delete-by-category",
		middleware.HeiCheckPermission([]string{"sys:log:remove"}),
		middleware.NoRepeat(5000),
		logDeleteByCategory,
	)

	// GET /api/v1/sys/log/vis/line-chart-data
	r.GET("/api/v1/sys/log/vis/line-chart-data",
		middleware.HeiCheckPermission([]string{"sys:log:page"}),
		logVisLineChart,
	)

	// GET /api/v1/sys/log/vis/pie-chart-data
	r.GET("/api/v1/sys/log/vis/pie-chart-data",
		middleware.HeiCheckPermission([]string{"sys:log:page"}),
		logVisPieChart,
	)

	// GET /api/v1/sys/log/op/bar-chart-data
	r.GET("/api/v1/sys/log/op/bar-chart-data",
		middleware.HeiCheckPermission([]string{"sys:log:page"}),
		logOpBarChart,
	)

	// GET /api/v1/sys/log/op/pie-chart-data
	r.GET("/api/v1/sys/log/op/pie-chart-data",
		middleware.HeiCheckPermission([]string{"sys:log:page"}),
		logOpPieChart,
	)
}

// logPage handles GET /api/v1/sys/log/page
func logPage(c *gin.Context) {
	param := &logPackage.LogPageParam{}
	if err := c.ShouldBindQuery(param); err != nil {
		param.Current = 1
		param.Size = 10
	}
	c.JSON(http.StatusOK, logPackage.Page(c, param))
}

// logCreate handles POST /api/v1/sys/log/create
func logCreate(c *gin.Context) {
	vo := &logPackage.LogVO{}
	if err := c.ShouldBindJSON(vo); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	logPackage.Create(c, vo, userID)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

// logModify handles POST /api/v1/sys/log/modify
func logModify(c *gin.Context) {
	vo := &logPackage.LogVO{}
	if err := c.ShouldBindJSON(vo); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if vo.ID == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	userID := auth.GetLoginIDDefaultNull(c)
	logPackage.Modify(c, vo, userID)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

// logRemove handles POST /api/v1/sys/log/remove
func logRemove(c *gin.Context) {
	param := &pojo.IdsParam{}
	if err := c.ShouldBindJSON(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if len(param.IDs) == 0 {
		c.JSON(http.StatusOK, result.Failure(c, "ids不能为空", 400, nil))
		return
	}

	logPackage.Remove(c, param.IDs)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

// logDetail handles GET /api/v1/sys/log/detail
func logDetail(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}

	data := logPackage.Detail(c, id)
	if data == nil {
		c.JSON(http.StatusOK, result.Success(c, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, data))
}

// logDeleteByCategory handles POST /api/v1/sys/log/delete-by-category
func logDeleteByCategory(c *gin.Context) {
	param := &logPackage.LogDeleteByCategoryParam{}
	if err := c.ShouldBindJSON(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "请求参数错误: "+err.Error(), 400, nil))
		return
	}
	if param.Category == "" {
		c.JSON(http.StatusOK, result.Failure(c, "category不能为空", 400, nil))
		return
	}

	logPackage.DeleteByCategory(c, param)
	c.JSON(http.StatusOK, result.Success(c, nil))
}

// logVisLineChart handles GET /api/v1/sys/log/vis/line-chart-data
func logVisLineChart(c *gin.Context) {
	data := logPackage.VisLineChart(c)
	c.JSON(http.StatusOK, result.Success(c, data))
}

// logVisPieChart handles GET /api/v1/sys/log/vis/pie-chart-data
func logVisPieChart(c *gin.Context) {
	data := logPackage.VisPieChart(c)
	c.JSON(http.StatusOK, result.Success(c, data))
}

// logOpBarChart handles GET /api/v1/sys/log/op/bar-chart-data
func logOpBarChart(c *gin.Context) {
	data := logPackage.OpBarChart(c)
	c.JSON(http.StatusOK, result.Success(c, data))
}

// logOpPieChart handles GET /api/v1/sys/log/op/pie-chart-data
func logOpPieChart(c *gin.Context) {
	data := logPackage.OpPieChart(c)
	c.JSON(http.StatusOK, result.Success(c, data))
}
