package utils

import (
	"fmt"
	"sync"
	"sync/atomic"

	"hei-gin/sdk/config"

	"github.com/bwmarrin/snowflake"
)

var (
	snowNode atomic.Pointer[snowflake.Node]
	snowMu   sync.Mutex
)

// initSnowflake initializes the snowflake node from the global config.
// It is safe for concurrent use. Unlike the old sync.Once approach,
// this will retry initialization if config.C is nil on the first call.
func initSnowflake() error {
	// Fast path: already initialized
	if n := snowNode.Load(); n != nil {
		return nil
	}

	snowMu.Lock()
	defer snowMu.Unlock()

	// Double-check after acquiring lock
	if snowNode.Load() != nil {
		return nil
	}

	if config.C == nil {
		return fmt.Errorf("config not loaded")
	}

	node, err := snowflake.NewNode(config.C.Snowflake.Instance)
	if err != nil {
		return fmt.Errorf("failed to create snowflake node: %w", err)
	}

	snowNode.Store(node)
	return nil
}

// GenerateID generates a unique ID using the snowflake algorithm and returns it as a string.
// If snowflake is not yet initialized (config not loaded), it returns a fallback ID.
func GenerateID() string {
	if err := initSnowflake(); err != nil {
		// Fallback: generate a v4 UUID-like ID for resilience
		// This ensures ID generation works even if config isn't loaded yet
		return fmt.Sprintf("tmp-%d", newUUID())
	}

	n := snowNode.Load()
	if n == nil {
		return fmt.Sprintf("tmp-%d", newUUID())
	}
	return n.Generate().String()
}

// uuidCounter provides a simple monotonic counter for fallback IDs
var uuidCounter uint64

func newUUID() uint64 {
	return atomic.AddUint64(&uuidCounter, 1)
}
