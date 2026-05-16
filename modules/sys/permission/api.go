package permission

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/permission/modules",
		auth.CheckPermission("sys:permission:modules"),
		GetModulesHandler,
	)
	r.GET("/api/v1/sys/permission/by-module",
		auth.CheckPermission("sys:permission:by-module"),
		GetPermissionsByModuleHandler,
	)
}

func GetModulesHandler(c *gin.Context) {
	modules, err := GetModules()
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}
	result.Success(c, modules)
}

func GetPermissionsByModuleHandler(c *gin.Context) {
	var req PermissionByModuleReq
	if err := c.ShouldBindQuery(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	items, err := GetPermissionsByModule(req.Module)
	if err != nil {
		result.Failure(c, "查询失败", 500)
		return
	}

	var vos []PermissionVO
	for _, item := range items {
		vos = append(vos, toVO(item))
	}
	result.Success(c, vos)
}
