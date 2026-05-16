package utils

import (
	"path/filepath"
	"runtime"
	"strings"
	"sync"

	"github.com/gin-gonic/gin"
	xdb "github.com/lionsoul2014/ip2region/binding/golang/xdb"
)

const unknown = "unknown"

var (
	searcher   *xdb.Searcher
	searcherMu sync.Mutex
)

// getSearcher returns the lazy-init singleton ip2region searcher.
func getSearcher() *xdb.Searcher {
	searcherMu.Lock()
	defer searcherMu.Unlock()

	if searcher != nil {
		return searcher
	}

	_, filename, _, _ := runtime.Caller(0)
	dir := filepath.Dir(filename)
	dbPath := filepath.Join(dir, "ip2region.xdb")

	cBuff, err := xdb.LoadContentFromFile(dbPath)
	if err != nil {
		return nil
	}

	s, err := xdb.NewWithBuffer(xdb.IPv4, cBuff)
	if err != nil {
		return nil
	}
	searcher = s
	return searcher
}

// GetClientIP extracts the real client IP from request headers,
// checking X-Forwarded-For, X-Real-IP, and Proxy-Client-IP in order.
func GetClientIP(c *gin.Context) string {
	for _, header := range []string{"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"} {
		ip := c.GetHeader(header)
		if ip != "" && ip != unknown {
			parts := strings.Split(ip, ",")
			return strings.TrimSpace(parts[0])
		}
	}
	return c.RemoteIP()
}

// GetCityInfo looks up city information for an IP address using the ip2region offline database.
// Returns a string like "中国|江苏|苏州" or "" on failure.
func GetCityInfo(ip string) string {
	if ip == "" || ip == unknown || ip == "127.0.0.1" || ip == "::1" {
		return ""
	}

	s := getSearcher()
	if s == nil {
		return ""
	}

	region, err := s.Search(ip)
	if err != nil {
		return ""
	}

	result := strings.ReplaceAll(region, "0|", "")
	result = strings.ReplaceAll(result, "|0", "")
	return result
}
