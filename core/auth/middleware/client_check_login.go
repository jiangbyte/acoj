package middleware

import (
	"github.com/gin-gonic/gin"
)

// HeiClientCheckLogin returns a middleware that checks if the CONSUMER user is logged in.
func HeiClientCheckLogin() gin.HandlerFunc {
	return HeiCheckLogin("CONSUMER")
}
