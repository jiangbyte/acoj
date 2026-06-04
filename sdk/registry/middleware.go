package registry

import "github.com/gin-gonic/gin"

// MiddlewareRegistrar is a function that registers one or more global middlewares.
type MiddlewareRegistrar func(r *gin.Engine)

var middlewares []MiddlewareRegistrar

// RegisterMiddleware registers a middleware registrar function.
// Call this from module init() to self-register global middleware.
// The registrations are applied in order, after the built-in middlewares.
func RegisterMiddleware(reg MiddlewareRegistrar) {
	middlewares = append(middlewares, reg)
}

// ApplyMiddlewares applies all registered global middlewares.
// Called from app.go after the built-in middleware chain.
func ApplyMiddlewares(r *gin.Engine) {
	for _, reg := range middlewares {
		reg(r)
	}
}
