package registry

import "github.com/gin-gonic/gin"

var middlewares []func(r *gin.Engine)

func RegisterMiddleware(reg func(r *gin.Engine)) {
	middlewares = append(middlewares, reg)
}

func ApplyMiddlewares(r *gin.Engine) {
	for _, reg := range middlewares {
		reg(r)
	}
}
