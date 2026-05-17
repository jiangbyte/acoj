package utils

import (
	"fmt"
	"sync"

	"hei-gin/config"

	"github.com/bwmarrin/snowflake"
)

var (
	snowNode    *snowflake.Node
	snowOnce    sync.Once
	snowInitErr error
)

// initSnowflake initializes the snowflake node from the global config.
// It is safe for concurrent use and runs only once.
func initSnowflake() {
	snowOnce.Do(func() {
		if config.C == nil {
			snowInitErr = fmt.Errorf("config not loaded")
			return
		}
		node, err := snowflake.NewNode(config.C.Snowflake.Instance)
		if err != nil {
			snowInitErr = fmt.Errorf("failed to create snowflake node: %w", err)
			return
		}
		snowNode = node
	})
}

// GenerateID generates a unique ID using the snowflake algorithm and returns it as a string.
func GenerateID() string {
	initSnowflake()
	if snowNode == nil {
		return ""
	}
	return snowNode.Generate().String()
}
