package banner

import (
	"github.com/gin-gonic/gin"
	"hei-gin/core/auth"
	authmw "hei-gin/core/auth/middleware"
	bizerr "hei-gin/core/exception"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
)

var s = &Service{}

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/banner/page", auth.CheckPermission("sys:banner:page"), Page)
	r.POST("/api/v1/sys/banner/create", auth.CheckPermission("sys:banner:create"), log.SysLog("添加Banner"), authmw.NoRepeat(3000), Create)
	r.POST("/api/v1/sys/banner/modify", auth.CheckPermission("sys:banner:modify"), log.SysLog("编辑Banner"), Modify)
	r.POST("/api/v1/sys/banner/remove", auth.CheckPermission("sys:banner:remove"), log.SysLog("删除Banner"), Remove)
	r.GET("/api/v1/sys/banner/detail", auth.CheckPermission("sys:banner:detail"), Detail)
}

func Page(c *gin.Context) {
	var param BannerPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		result.ValidationError(c, err)
		return
	}
	if param.Current <= 0 {
		param.Current = 1
	}
	if param.Size <= 0 {
		param.Size = 20
	}
	bounds := &pojo.PageBounds{Current: param.Current, Size: param.Size}
	records, total, err := s.FindPage(c.Request.Context(), bounds)
	if err != nil {
		panic(bizerr.NewBusinessError(err.Error()))
	}
	result.Page(c, records, int64(total), param.Current, param.Size)
}

func Create(c *gin.Context) {
	var vo BannerVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		result.ValidationError(c, err)
		return
	}
	data, err := s.Create(c.Request.Context(), &vo)
	if err != nil {
		panic(bizerr.NewBusinessError(err.Error()))
	}
	result.Success(c, data)
}

func Modify(c *gin.Context) {
	var vo BannerVO
	if err := c.ShouldBindJSON(&vo); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := s.Modify(c.Request.Context(), &vo); err != nil {
		panic(bizerr.NewBusinessError(err.Error()))
	}
	result.Success(c, nil)
}

func Remove(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		result.ValidationError(c, err)
		return
	}
	if err := s.Remove(c.Request.Context(), param.IDs); err != nil {
		panic(bizerr.NewBusinessError(err.Error()))
	}
	result.Success(c, nil)
}

func Detail(c *gin.Context) {
	id := c.Query("id")
	data, err := s.FindByID(c.Request.Context(), id)
	if err != nil {
		panic(bizerr.NewBusinessError(err.Error()))
	}
	result.Success(c, data)
}
