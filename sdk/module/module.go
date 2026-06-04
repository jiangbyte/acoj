package module

import "log"

// Module defines the lifecycle of an app module.
type Module interface {
	// Name returns the module name for logging.
	Name() string
	// Init is called during app startup, after config and DB are ready, before the HTTP server starts.
	Init() error
	// Start is called after the HTTP server starts (for background tasks like cron).
	Start() error
	// Stop is called during graceful shutdown.
	Stop() error
}

// NoopModule can be embedded to avoid implementing all methods.
type NoopModule struct{}

func (NoopModule) Init() error  { return nil }
func (NoopModule) Start() error { return nil }
func (NoopModule) Stop() error  { return nil }

var modules []Module

// Register registers a module. Call this from init() to self-register.
func Register(m Module) {
	modules = append(modules, m)
	log.Printf("[module] registered: %s", m.Name())
}

// InitAll runs Init() on all registered modules in registration order.
func InitAll() error {
	for _, m := range modules {
		log.Printf("[module] init: %s", m.Name())
		if err := m.Init(); err != nil {
			return err
		}
	}
	return nil
}

// StartAll runs Start() on all registered modules.
func StartAll() {
	for _, m := range modules {
		if err := m.Start(); err != nil {
			log.Printf("[module] %s start error: %v", m.Name(), err)
		}
	}
}

// StopAll runs Stop() on all registered modules in reverse order.
func StopAll() {
	for i := len(modules) - 1; i >= 0; i-- {
		m := modules[i]
		if err := m.Stop(); err != nil {
			log.Printf("[module] %s stop error: %v", m.Name(), err)
		}
	}
}
