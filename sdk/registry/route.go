package registry

import "github.com/gin-gonic/gin"

// RouteRegistrar is a function that registers routes on the given gin engine.
type RouteRegistrar func(r *gin.Engine)

var registrars []RouteRegistrar

// RegisterRoute registers a route registrar function.
// Call this from module init() to self-register.
func RegisterRoute(reg RouteRegistrar) {
	registrars = append(registrars, reg)
}

// ExecuteRoutes runs all registered route registrars.
func ExecuteRoutes(r *gin.Engine) {
	for _, reg := range registrars {
		reg(r)
	}
}
