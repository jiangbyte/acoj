package v1

import (
	dict "hei-gin/modules/sys/dict"

	"github.com/gin-gonic/gin"
)

// RegisterPublicRoutes registers public dict routes (no auth required).
func RegisterPublicRoutes(r *gin.Engine) {
	// GET /api/v1/public/biz-dict/tree — public BIZ dictionary tree
	r.GET("/api/v1/public/biz-dict/tree", publicBizDictTree)
}

// publicBizDictTree handles GET /api/v1/public/biz-dict/tree
func publicBizDictTree(c *gin.Context) {
	data := dict.Tree(c, &dict.DictTreeParam{DictGroup: "BIZ"})
	c.JSON(200, gin.H{
		"code":    200,
		"message": "请求成功",
		"data":    data,
	})
}
