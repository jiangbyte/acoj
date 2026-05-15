package utils

import (
	"github.com/bwmarrin/snowflake"

	"hei-gin/config"
)

var node *snowflake.Node

func InitSnowflake() error {
	var err error
	node, err = snowflake.NewNode(config.C.Snowflake.Instance)
	return err
}

func NextID() string {
	if node == nil {
		return ""
	}
	return node.Generate().String()
}
