package v1

import (
	"hei-gin/core/auth/middleware"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	"hei-gin/core/registry"
	file "hei-gin/modules/sys/file"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all file routes on the given gin engine.
func RegisterRoutes(r *gin.Engine) {
	// POST /api/v1/sys/file/upload
	r.POST("/api/v1/sys/file/upload",
		registry.Perm("sys:file:upload", "上传文件"),
		log.SysLog("上传文件"),
		fileUpload,
	)

	// GET /api/v1/sys/file/download
	r.GET("/api/v1/sys/file/download",
		registry.Perm("sys:file:download", "下载文件"),
		fileDownload,
	)

	// GET /api/v1/sys/file/page
	r.GET("/api/v1/sys/file/page",
		registry.Perm("sys:file:page", "文件分页"),
		filePage,
	)

	// GET /api/v1/sys/file/detail
	r.GET("/api/v1/sys/file/detail",
		registry.Perm("sys:file:detail", "文件详情"),
		fileDetail,
	)

	// POST /api/v1/sys/file/remove
	r.POST("/api/v1/sys/file/remove",
		registry.Perm("sys:file:remove", "删除文件"),
		log.SysLog("删除文件"),
		fileRemove,
	)

	// POST /api/v1/sys/file/remove-absolute
	r.POST("/api/v1/sys/file/remove-absolute",
		registry.Perm("sys:file:remove", "删除文件"),
		log.SysLog("物理删除文件"),
		fileRemoveAbsolute,
	)
}

// RegisterClientRoutes registers consumer file routes (C端, login required).
func RegisterClientRoutes(r *gin.Engine) {
	// POST /api/v1/c/file/upload — consumer file upload
	r.POST("/api/v1/c/file/upload",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端上传文件"),
		clientFileUpload,
	)
}

// fileUpload handles POST /api/v1/sys/file/upload
func fileUpload(c *gin.Context) {
	data := file.Upload(c)
	c.JSON(200, result.Success(c, data))
}

// clientFileUpload handles POST /api/v1/c/file/upload
func clientFileUpload(c *gin.Context) {
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
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	file.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// fileRemoveAbsolute handles POST /api/v1/sys/file/remove-absolute
func fileRemoveAbsolute(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}

	file.RemoveAbsolute(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}
