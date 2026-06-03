package v1

import (
	"hei-gin/core/auth"
	"hei-gin/core/log"
	"hei-gin/core/pojo"
	"hei-gin/core/result"
	problem "hei-gin/modules/judge/problem"

	"github.com/gin-gonic/gin"
)

// RegisterRoutes registers admin routes (requires admin auth via AuthCheck middleware).
func RegisterRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/judge/problem")
	g.POST("/create",
		log.SysLog("创建题目"),
		createHandler,
	)
	g.POST("/modify",
		log.SysLog("编辑题目"),
		modifyHandler,
	)
	g.POST("/remove",
		log.SysLog("删除题目"),
		deleteHandler,
	)
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.GET("/languages", listLanguagesHandler)
	g.GET("/testcases", listTestcasesHandler)
	g.GET("/subtasks", listSubtasksHandler)

	// Testcase management
	g.POST("/testcase/add",
		log.SysLog("添加测试点"),
		testcaseAddHandler,
	)
	g.POST("/testcase/modify",
		log.SysLog("修改测试点"),
		testcaseModifyHandler,
	)
	g.POST("/testcase/remove",
		log.SysLog("删除测试点"),
		testcaseRemoveHandler,
	)

	// Language management
	g.POST("/language/sync",
		log.SysLog("同步语言配置"),
		languageSyncHandler,
	)

	// Sample management
	g.POST("/sample/add",
		log.SysLog("添加样例"),
		sampleAddHandler,
	)
	g.POST("/sample/remove",
		log.SysLog("删除样例"),
		sampleRemoveHandler,
	)

	// Subtask management
	g.POST("/subtask/add",
		log.SysLog("添加子任务"),
		subtaskAddHandler,
	)
	g.POST("/subtask/modify",
		log.SysLog("修改子任务"),
		subtaskModifyHandler,
	)
	g.POST("/subtask/remove",
		log.SysLog("删除子任务"),
		subtaskRemoveHandler,
	)
	g.POST("/subtask/dep/add",
		log.SysLog("添加子任务依赖"),
		depAddHandler,
	)
	g.POST("/subtask/dep/remove",
		log.SysLog("删除子任务依赖"),
		depRemoveHandler,
	)
}

// RegisterPublicRoutes registers public routes (no auth required).
func RegisterPublicRoutes(r *gin.Engine) {
	g := r.Group("/api/v1/public/judge/problem")
	g.GET("/page", pageHandler)
	g.GET("/detail", detailHandler)
	g.GET("/languages", listLanguagesHandler)
}

func pageHandler(c *gin.Context) {
	var param problem.ProblemPageParam
	if err := c.ShouldBindQuery(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	data := problem.Page(c, &param)
	c.JSON(200, data)
}

func createHandler(c *gin.Context) {
	var param problem.ProblemCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	problem.Create(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param problem.ProblemModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	userID := auth.GetLoginIDDefaultNull(c)
	problem.Modify(c, &param, userID)
	c.JSON(200, result.Success(c, nil))
}

func deleteHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.Remove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func detailHandler(c *gin.Context) {
	id := c.Query("id")
	vo := problem.Detail(c, id)
	if vo == nil {
		c.JSON(200, result.Success(c, nil))
		return
	}
	c.JSON(200, result.Success(c, vo))
}

func listLanguagesHandler(c *gin.Context) {
	id := c.Query("id")
	data := problem.ListLanguages(c, id)
	c.JSON(200, data)
}

func listTestcasesHandler(c *gin.Context) {
	id := c.Query("id")
	data := problem.ListTestcases(c, id)
	c.JSON(200, data)
}

func listSubtasksHandler(c *gin.Context) {
	id := c.Query("id")
	data := problem.ListSubtasks(c, id)
	c.JSON(200, data)
}

// ===== Testcase Management Handlers =====

func testcaseAddHandler(c *gin.Context) {
	var param problem.TestcaseAddParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.TestcaseAdd(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func testcaseModifyHandler(c *gin.Context) {
	var param problem.TestcaseModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.TestcaseModify(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func testcaseRemoveHandler(c *gin.Context) {
	var param problem.TestcaseRemoveParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.TestcaseRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// ===== Language Management Handlers =====

func languageSyncHandler(c *gin.Context) {
	var param problem.LanguageSyncParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.LanguageSync(c, &param)
	c.JSON(200, result.Success(c, nil))
}

// ===== Sample Management Handlers =====

func sampleAddHandler(c *gin.Context) {
	var param problem.SampleAddParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.SampleAdd(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func sampleRemoveHandler(c *gin.Context) {
	var param problem.SampleRemoveParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.SampleRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

// ===== Subtask Management Handlers =====

func subtaskAddHandler(c *gin.Context) {
	var param problem.SubtaskAddParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.SubtaskAdd(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func subtaskModifyHandler(c *gin.Context) {
	var param problem.SubtaskModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.SubtaskModify(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func subtaskRemoveHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.SubtaskRemove(c, param.IDs)
	c.JSON(200, result.Success(c, nil))
}

func depAddHandler(c *gin.Context) {
	var param problem.DepAddParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.DepAdd(c, &param)
	c.JSON(200, result.Success(c, nil))
}

func depRemoveHandler(c *gin.Context) {
	var param struct {
		ID string `json:"id"`
	}
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(200, result.Failure(c, "参数错误: "+err.Error(), 400, nil))
		return
	}
	problem.DepRemove(c, param.ID)
	c.JSON(200, result.Success(c, nil))
}
