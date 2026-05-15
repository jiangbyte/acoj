package file

import (
	"fmt"

	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.POST("/api/v1/sys/file/upload",
		log.SysLog("上传文件"),
		auth.CheckPermission("sys:file:upload"),
		UploadHandler,
	)
	r.GET("/api/v1/sys/file/page",
		auth.CheckPermission("sys:file:page"),
		PageHandler,
	)
	r.POST("/api/v1/sys/file/remove",
		log.SysLog("删除文件"),
		auth.CheckPermission("sys:file:remove"),
		RemoveHandler,
	)
	r.GET("/api/v1/sys/file/download",
		auth.CheckPermission("sys:file:download"),
		DownloadHandler,
	)
	r.GET("/api/v1/sys/file/detail",
		auth.CheckPermission("sys:file:detail"),
		DetailHandler,
	)
	r.POST("/api/v1/sys/file/remove-absolute",
		log.SysLog("物理删除文件"),
		auth.CheckPermission("sys:file:remove"),
		RemoveAbsoluteHandler,
	)
}

func UploadHandler(c *gin.Context) {
	file, header, err := c.Request.FormFile("file")
	if err != nil {
		result.Failure(c, "请上传文件", 400)
		return
	}
	defer file.Close()

	loginID := auth.AuthTool.GetLoginID(c)
	item, err := Upload(file, header, loginID)
	if err != nil {
		result.Failure(c, "上传失败: "+err.Error(), 500)
		return
	}
	result.Success(c, toVO(item))
}

func PageHandler(c *gin.Context) {
	var p PageParam
	if err := c.ShouldBindQuery(&p); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}
	if p.Page <= 0 {
		p.Page = 1
	}
	if p.Size <= 0 {
		p.Size = 10
	}

	total, items, err := Page(p.Page, p.Size, p.Keyword)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []SysFileVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Page(c, vos, int64(total), p.Page, p.Size)
}

func RemoveHandler(c *gin.Context) {
	var req RemoveReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := Remove(req.IDs); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}

func DownloadHandler(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	item, err := Download(id)
	if err != nil {
		result.Failure(c, "文件不存在", 404)
		return
	}

	c.Header("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, item.OriginalName))
	c.File(item.Path)
}

func DetailHandler(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	item, err := Detail(id)
	if err != nil {
		result.Failure(c, "文件不存在", 404)
		return
	}
	result.Success(c, toVO(item))
}

func RemoveAbsoluteHandler(c *gin.Context) {
	var req RemoveReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.Failure(c, "请求参数格式错误", 400)
		return
	}

	if err := RemoveAbsolute(req.IDs); err != nil {
		result.Failure(c, "删除失败", 500)
		return
	}
	result.Success(c, nil)
}
