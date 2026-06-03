package v1

import (
	"hei-gin/core/result"
	sandbox "hei-gin/modules/judge/sandbox"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/judge/sandbox")
	g.POST("/register", registerHandler)
	g.POST("/heartbeat", heartbeatHandler)
	g.POST("/unregister", unregisterHandler)
	g.GET("/instances", instancesHandler)
}

func registerHandler(c *gin.Context) {
	var param sandbox.RegisterParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	sandbox.Register(c, &param)
	c.JSON(200, result.Success(c, gin.H{"status": "ACTIVE"}))
}

func heartbeatHandler(c *gin.Context) {
	var param sandbox.HeartbeatParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	sandbox.Heartbeat(c, param.Addr)
	c.JSON(200, result.Success(c, gin.H{"status": "ACTIVE"}))
}

func unregisterHandler(c *gin.Context) {
	var param sandbox.HeartbeatParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	sandbox.Unregister(c, param.Addr)
	c.JSON(200, result.Success(c, gin.H{"status": "REMOVED"}))
}

func instancesHandler(c *gin.Context) {
	data := sandbox.ListInstances(c)
	c.JSON(200, result.Success(c, data))
}
