package auth

// HeiPermissionInterfaceManager manages the registered HeiPermissionInterface implementation.
type HeiPermissionInterfaceManager struct{}

var (
	_permissionInterface HeiPermissionInterface
)

// RegisterInterface registers the permission interface implementation.
func RegisterInterface(iface HeiPermissionInterface) {
	_permissionInterface = iface
}

// GetInterface returns the registered permission interface, or nil.
func GetInterface() HeiPermissionInterface {
	return _permissionInterface
}

// HasInterface returns whether a permission interface has been registered.
func HasInterface() bool {
	return _permissionInterface != nil
}
