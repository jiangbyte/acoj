package v1

import (
	"net/http"

	"hei-gin/sdk/registry"
	"hei-gin/sdk/result"

	tag "hei-gin/plugins/plugin-judge/tag"

	"github.com/gin-gonic/gin"
)

// RegisterPublicRoutes registers public (C-end accessible) tag routes.
func RegisterPublicRoutes(r *gin.Engine) {
	r.GET("/api/v1/public/c/judge/tag/list-all", publicListAllHandler)
}

func publicListAllHandler(c *gin.Context) {
	tags, err := tag.ListAllService(c)
	if err != nil {
		c.JSON(http.StatusOK, result.Failure(c, err.Error(), 500, nil))
		return
	}
	c.JSON(http.StatusOK, result.Success(c, tags))
}

func init() {
	registry.RegisterRoute(RegisterPublicRoutes)
}
