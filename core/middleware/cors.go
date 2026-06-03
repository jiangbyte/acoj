package middleware

import (
	"hei-gin/config"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

// CORS returns a Gin middleware that configures Cross-Origin Resource Sharing.
func CORS() gin.HandlerFunc {
	return cors.New(cors.Config{
		AllowOrigins:     config.C.CORS.AllowOrigins,
		AllowMethods:     config.C.CORS.AllowMethods,
		AllowHeaders:     config.C.CORS.AllowHeaders,
		AllowCredentials: config.C.CORS.AllowCredentials,
	})
}
