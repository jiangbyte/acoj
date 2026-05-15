package auth

import "sync"

var (
	globalPermissionInterface *PermissionInterface
	piMu                      sync.RWMutex
)

// RegisterPermissionInterface 注册权限接口实现
func RegisterPermissionInterface(pi *PermissionInterface) {
	piMu.Lock()
	defer piMu.Unlock()
	globalPermissionInterface = pi
}

// GetPermissionInterface 获取已注册的权限接口
func GetPermissionInterface() *PermissionInterface {
	piMu.RLock()
	defer piMu.RUnlock()
	return globalPermissionInterface
}
