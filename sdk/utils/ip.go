package utils

import (
	"log"
	"strings"

	"github.com/gin-gonic/gin"
)

var ipSearcher interface{}

func InitIPDB(dbPath string) {
	if dbPath == "" {
		log.Println("[IP] No IP2Region db path configured, city lookup disabled")
		return
	}
	log.Printf("[IP] IP2Region disabled in sdk (will be enabled in plugin)")
}

func GetClientIP(c *gin.Context) string {
	ip := c.GetHeader("X-Forwarded-For")
	if ip != "" {
		parts := strings.Split(ip, ",")
		return strings.TrimSpace(parts[0])
	}
	ip = c.GetHeader("X-Real-IP")
	if ip != "" {
		return ip
	}
	return c.ClientIP()
}

func GetCityInfo(ip string) string {
	return "-"
}
