package home

import (
	"github.com/gin-gonic/gin"

	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/result"
)

func RegisterRoutes(r *gin.RouterGroup) {
	r.GET("/api/v1/sys/home", HomeHandler)
	r.POST("/api/v1/sys/home/quick-actions/add",
		log.SysLog("添加快捷方式"),
		AddQuickActionHandler,
	)
	r.POST("/api/v1/sys/home/quick-actions/remove",
		log.SysLog("移除快捷方式"),
		RemoveQuickActionHandler,
	)
	r.POST("/api/v1/sys/home/quick-actions/sort",
		log.SysLog("排序快捷方式"),
		SortQuickActionHandler,
	)
}

func HomeHandler(c *gin.Context) {
	loginID := auth.AuthTool.GetLoginID(c)

	data, err := GetHomeData(loginID)
	if err != nil {
		result.Failure(c, "获取首页数据失败", 500)
		return
	}
	result.Success(c, data)
}

func AddQuickActionHandler(c *gin.Context) {
	var req AddQuickActionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	if err := AddQuickAction(loginID, req.ResourceID); err != nil {
		result.Failure(c, "添加快捷方式失败", 500)
		return
	}
	result.Success(c, nil)
}

func RemoveQuickActionHandler(c *gin.Context) {
	var req RemoveQuickActionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	if err := RemoveQuickAction(loginID, req.ID); err != nil {
		result.Failure(c, "移除快捷方式失败", 500)
		return
	}
	result.Success(c, nil)
}

func SortQuickActionHandler(c *gin.Context) {
	var req SortQuickActionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		result.ValidationError(c, err)
		return
	}

	loginID := auth.AuthTool.GetLoginID(c)
	if err := SortQuickActions(loginID, req.IDs); err != nil {
		result.Failure(c, "排序快捷方式失败", 500)
		return
	}
	result.Success(c, nil)
}
