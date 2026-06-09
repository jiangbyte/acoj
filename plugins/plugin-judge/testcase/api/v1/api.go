package v1

import (
	"net/http"

	"hei-gin/sdk/pojo"
	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	testcase "hei-gin/plugins/plugin-judge/testcase"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine) {
	r.GET("/api/v1/judge/testcase/list",
		registry.Perm("judge:testcase:list", "测试用例列表"),
		listHandler,
	)
	r.GET("/api/v1/judge/testcase/content",
		registry.Perm("judge:testcase:content", "测试用例内容"),
		contentHandler,
	)
	r.POST("/api/v1/judge/testcase/create",
		registry.Perm("judge:testcase:create", "创建测试用例"),
		createHandler,
	)
	r.POST("/api/v1/judge/testcase/batch-create",
		registry.Perm("judge:testcase:batch-create", "批量创建测试用例"),
		batchCreateHandler,
	)
	r.POST("/api/v1/judge/testcase/modify",
		registry.Perm("judge:testcase:modify", "编辑测试用例"),
		modifyHandler,
	)
	r.POST("/api/v1/judge/testcase/remove",
		registry.Perm("judge:testcase:remove", "删除测试用例"),
		removeHandler,
	)
}

func listHandler(c *gin.Context) {
	problemID := c.Query("problem_id")
	if problemID == "" {
		c.JSON(http.StatusOK, result.Failure(c, "problem_id不能为空", 400, nil))
		return
	}
	c.JSON(http.StatusOK, testcase.ListByProblemService(c, problemID))
}

func contentHandler(c *gin.Context) {
	id := c.Query("id")
	if id == "" {
		c.JSON(http.StatusOK, result.Failure(c, "id不能为空", 400, nil))
		return
	}
	field := c.DefaultQuery("field", "")
	vo, err := testcase.GetContentService(c, id, field)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, vo))
}

func createHandler(c *gin.Context) {
	var param testcase.TestcaseCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := testcase.CreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func batchCreateHandler(c *gin.Context) {
	var param testcase.TestcaseBatchCreateParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := testcase.BatchCreateService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func modifyHandler(c *gin.Context) {
	var param testcase.TestcaseModifyParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := testcase.ModifyService(c, &param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func removeHandler(c *gin.Context) {
	var param pojo.IdsParam
	if err := c.ShouldBindJSON(&param); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, "参数错误", 400, nil))
		return
	}
	if err := testcase.RemoveService(c, testcase.TestcaseRemoveParam(param)); err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, nil))
}

func init() {
	registry.RegisterRoute(RegisterRoutes)
}
