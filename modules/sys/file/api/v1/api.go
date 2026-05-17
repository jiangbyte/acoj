package v1

import (
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/result"
	file "hei-gin/modules/sys/file"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all file routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// POST /api/v1/sys/file/upload
	r.POST("/api/v1/sys/file/upload",
		middleware.HeiCheckPermission([]string{"sys:file:upload"}),
		log.SysLog("上传文件"),
		fileUpload,
	)

	// GET /api/v1/sys/file/download
	r.GET("/api/v1/sys/file/download",
		middleware.HeiCheckPermission([]string{"sys:file:download"}),
		fileDownload,
	)

	// GET /api/v1/sys/file/page
	r.GET("/api/v1/sys/file/page",
		middleware.HeiCheckPermission([]string{"sys:file:page"}),
		filePage,
	)

	// GET /api/v1/sys/file/detail
	r.GET("/api/v1/sys/file/detail",
		middleware.HeiCheckPermission([]string{"sys:file:detail"}),
		fileDetail,
	)

	// POST /api/v1/sys/file/remove
	r.POST("/api/v1/sys/file/remove",
		middleware.HeiCheckPermission([]string{"sys:file:remove"}),
		log.SysLog("删除文件"),
		fileRemove,
	)

	// POST /api/v1/sys/file/remove-absolute
	r.POST("/api/v1/sys/file/remove-absolute",
		middleware.HeiCheckPermission([]string{"sys:file:remove"}),
		log.SysLog("物理删除文件"),
		fileRemoveAbsolute,
	)
}

// fileUpload handles POST /api/v1/sys/file/upload
func fileUpload(c *gin.Context) {
	data := file.Upload(c)
	c.JSON(200, result.Success(c, data))
}

// fileDownload handles GET /api/v1/sys/file/download
func fileDownload(c *gin.Context) {
	id := c.Query("id")
	file.Download(c, id)
}

// filePage handles GET /api/v1/sys/file/page
func filePage(c *gin.Context) {
	var param file.FilePageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	data := file.Page(c, &param)
	c.JSON(200, data)
}

// fileDetail handles GET /api/v1/sys/file/detail
func fileDetail(c *gin.Context) {
	id := c.Query("id")
	vo := file.Detail(c, id)
	c.JSON(200, result.Success(c, vo))
}

// fileRemove handles POST /api/v1/sys/file/remove
func fileRemove(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	file.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// fileRemoveAbsolute handles POST /api/v1/sys/file/remove-absolute
func fileRemoveAbsolute(c *gin.Context) {
	var param struct {
		IDs []string `json:"ids"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	file.RemoveAbsolute(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}
