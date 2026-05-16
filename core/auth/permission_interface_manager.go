package auth

// PermissionInterfaceManager is a registry for the permission interface implementation,
// matching fastapi's HeiPermissionInterfaceManager.
type PermissionInterfaceManager struct{}

// PermissionInterfaceManagerInstance is the global manager instance.
var PermissionInterfaceManagerInstance = &PermissionInterfaceManager{}

// RegisterInterface registers the permission interface implementation.
func (m *PermissionInterfaceManager) RegisterInterface(iface *PermissionInterfaceImpl) {
	PermissionInterface = iface
}

// GetInterface returns the current permission interface implementation.
func (m *PermissionInterfaceManager) GetInterface() *PermissionInterfaceImpl {
	return PermissionInterface
}

// HasInterface returns whether an interface has been registered.
func (m *PermissionInterfaceManager) HasInterface() bool {
	return PermissionInterface != nil
}
