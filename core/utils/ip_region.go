package utils

import (
	"log"
	"os"
	"path/filepath"
	"strings"
	"sync"

	"github.com/lionsoul2014/ip2region/binding/golang/xdb"
)

var (
	IP2RegionDBPath = filepath.Join("core", "utils", "ip2region.xdb")
	searcherOnce    sync.Once
	searcher        *xdb.Searcher
	searcherErr     error
)

func getSearcher() *xdb.Searcher {
	searcherOnce.Do(func() {
		if _, err := os.Stat(IP2RegionDBPath); os.IsNotExist(err) {
			searcherErr = err
			return
		}
		cBuff, err := os.ReadFile(IP2RegionDBPath)
		if err != nil {
			searcherErr = err
			return
		}
		version, err := xdb.VersionFromName("IPv4")
		if err != nil {
			searcherErr = err
			return
		}
		s, err := xdb.NewWithBuffer(version, cBuff)
		if err != nil {
			searcherErr = err
			return
		}
		searcher = s
	})
	return searcher
}

// GetCityInfo looks up city info for an IP address using ip2region offline database.
// Returns a string like "中国|江苏|苏州" or empty string on failure.
func GetCityInfo(ip string) string {
	if ip == "" || ip == "unknown" || ip == "127.0.0.1" || ip == "::1" {
		return ""
	}

	s := getSearcher()
	if s == nil {
		return ""
	}

	region, err := s.Search(ip)
	if err != nil {
		log.Printf("[GetCityInfo] ip2region search failed for %s: %v", ip, err)
		return ""
	}

	// ip2region returns "国家|区域|省份|城市|ISP" with "0" for unknown parts.
	// Strip zero-value artifacts like fastapi does: region.replace("0|", "").replace("|0", "")
	region = strings.ReplaceAll(region, "0|", "")
	region = strings.TrimSuffix(strings.ReplaceAll(region, "|0", ""), "|")
	return region
}
