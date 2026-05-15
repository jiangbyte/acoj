package utility

import (
	"context"
	"github.com/bwmarrin/snowflake"
	"github.com/gogf/gf/v2/frame/g"
)

var node *snowflake.Node

func init() {
	instance := g.Cfg().MustGet(context.Background(), "hei.snowflake.instance", 1).Int()
	node, _ = snowflake.NewNode(int64(instance))
}

func GenerateID() string {
	return node.Generate().String()
}
