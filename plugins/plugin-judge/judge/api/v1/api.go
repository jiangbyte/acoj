package v1

import (
	"net/http"

	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	judge "hei-gin/plugins/plugin-judge/judge"
	"hei-gin/plugins/plugin-judge/sandbox"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/judge/sandbox/health",
		registry.Perm("judge:sandbox:health", "沙箱健康检查"),
		healthHandler,
	)
	r.POST("/api/v1/judge/sandbox/create",
		registry.Perm("judge:sandbox:create", "注册沙箱"),
		createSandboxHandler,
	)
	r.POST("/api/v1/judge/sandbox/modify",
		registry.Perm("judge:sandbox:modify", "编辑沙箱"),
		modifySandboxHandler,
	)
	r.POST("/api/v1/judge/sandbox/remove",
		registry.Perm("judge:sandbox:remove", "删除沙箱"),
		removeSandboxHandler,
	)
	r.GET("/api/v1/judge/config/list",
		registry.Perm("judge:config:list", "判题配置列表"),
		configListHandler,
	)
	r.POST("/api/v1/judge/config/update",
		registry.Perm("judge:config:update", "更新判题配置"),
		configUpdateHandler,
	)
}

func healthHandler(c *gin.Context) {
	backends := sandbox.DefaultPool.GetAll()
	var healthList []judge.SandboxHealthVO
	for _, b := range backends {
		h := b.Health()
		healthList = append(healthList, judge.SandboxHealthVO{
			BackendName: h.BackendName,
			Alive:       h.Alive,
			Version:     h.Version,
			Error:       h.Error,
		})
	}
	c.JSON(http.StatusOK, result.Success(c, healthList))
}

func createSandboxHandler(c *gin.Context) {
	var param judge.SandboxCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}

	sb := sandbox.JudgeSandbox{
		Name:     param.Name,
		Endpoint: param.Endpoint,
		Timeout:  param.Timeout,
		Status:   "active",
	}
	if sb.Timeout <= 0 {
		sb.Timeout = 30
	}

	if err := sandbox.CreateSandbox(&sb); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func modifySandboxHandler(c *gin.Context) {
	var param judge.SandboxModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := sandbox.ModifySandbox(param.ID, param.Name, param.Endpoint, param.Timeout); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func removeSandboxHandler(c *gin.Context) {
	var param judge.SandboxRemoveParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := sandbox.RemoveSandboxes(param.IDs); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func configListHandler(c *gin.Context) {
	configs, err := judge.GetAllConfigs()
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, configs))
}

func configUpdateHandler(c *gin.Context) {
	var param judge.JudgeConfigBatchUpdateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := judge.BatchUpdateConfig(param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func init() {
	registry.RegisterRoute(RegisterRoutes)
}
