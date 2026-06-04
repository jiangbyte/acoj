package v1

import (
	"strconv"

	"hei-gin/sdk/auth/middleware"
	"hei-gin/sdk/log"
	"hei-gin/sdk/pojo"
	"hei-gin/sdk/result"
	"hei-gin/sdk/registry"
	file "hei-gin/plugins/plugin-sys/file"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers all admin file routes.
func RegisterRoutes(r *gin.Engine) {
	// ---- Single-file upload ----
	r.POST("/api/v1/sys/file/upload",
		registry.Perm("sys:file:upload", "上传文件"),
		log.SysLog("上传文件"),
		fileUpload,
	)

	// ---- Chunked upload ----
	r.POST("/api/v1/sys/file/upload/init",
		registry.Perm("sys:file:upload", "分片上传-初始化"),
		log.SysLog("分片上传-初始化"),
		fileUploadInit,
	)
	r.POST("/api/v1/sys/file/upload/chunk",
		registry.Perm("sys:file:upload", "分片上传-上传分片"),
		log.SysLog("分片上传-上传分片"),
		fileUploadChunk,
	)
	r.POST("/api/v1/sys/file/upload/complete",
		registry.Perm("sys:file:upload", "分片上传-完成"),
		log.SysLog("分片上传-完成"),
		fileUploadComplete,
	)
	r.POST("/api/v1/sys/file/upload/abort",
		registry.Perm("sys:file:upload", "分片上传-取消"),
		log.SysLog("分片上传-取消"),
		fileUploadAbort,
	)

	// ---- Basic CRUD ----
	r.GET("/api/v1/sys/file/download",
		registry.Perm("sys:file:download", "下载文件"),
		fileDownload,
	)
	r.GET("/api/v1/sys/file/page",
		registry.Perm("sys:file:page", "文件分页"),
		filePage,
	)
	r.GET("/api/v1/sys/file/detail",
		registry.Perm("sys:file:detail", "文件详情"),
		fileDetail,
	)
	r.POST("/api/v1/sys/file/remove",
		registry.Perm("sys:file:remove", "删除文件"),
		log.SysLog("删除文件"),
		fileRemove,
	)
	r.POST("/api/v1/sys/file/remove-absolute",
		registry.Perm("sys:file:remove", "删除文件"),
		log.SysLog("物理删除文件"),
		fileRemoveAbsolute,
	)
}

// RegisterClientRoutes registers consumer file routes.
func RegisterClientRoutes(r *gin.Engine) {
	r.POST("/api/v1/c/file/upload",
		middleware.HeiClientCheckLogin(),
		log.SysLog("C端上传文件"),
		clientFileUpload,
	)
}

// ---- Handlers ----

func fileUpload(c *gin.Context) {
	data := file.Upload(c)
	c.JSON(200, result.Success(c, data))
}

func clientFileUpload(c *gin.Context) {
	data := file.Upload(c)
	c.JSON(200, result.Success(c, data))
}

func fileDownload(c *gin.Context) {
	id := c.Query("id")
	file.Download(c, id)
}

func filePage(c *gin.Context) {
	var param file.FilePageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := file.Page(c, &param)
	c.JSON(200, data)
}

func fileDetail(c *gin.Context) {
	id := c.Query("id")
	vo := file.Detail(c, id)
	c.JSON(200, result.Success(c, vo))
}

func fileRemove(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	file.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func fileRemoveAbsolute(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	file.RemoveAbsolute(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// ---- Chunked upload handlers ----

func fileUploadInit(c *gin.Context) {
	var param file.ChunkUploadInitParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := file.InitChunkUpload(c, &param)
	c.JSON(200, result.Success(c, data))
}

func fileUploadChunk(c *gin.Context) {
	chunkIndex, _ := strconv.Atoi(c.PostForm("chunk_index"))
	param := file.ChunkUploadParam{
		UploadID:   c.PostForm("upload_id"),
		ChunkIndex: chunkIndex,
		Checksum:   c.PostForm("checksum"),
	}
	if param.UploadID == "" {
		c.JSON(200, result.Failure(c, "upload_id 不能为空", 400, nil))
		return
	}
	file.UploadChunk(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func fileUploadComplete(c *gin.Context) {
	file.CompleteChunkUpload(c)
}

func fileUploadAbort(c *gin.Context) {
	file.AbortChunkUpload(c)
	c.JSON(200, result.Success(c, nil))
}

// Ensure fmt import is used
func init() {
	registry.RegisterRoute(RegisterRoutes)
	registry.RegisterRoute(RegisterClientRoutes)
}
