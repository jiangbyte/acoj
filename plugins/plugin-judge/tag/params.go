package tag

import "hei-gin/sdk/pojo"

type TagVO struct {
	ID        string `json:"id"`
	Name      string `json:"name"`
	Color     string `json:"color"`
	CreatedAt string `json:"created_at"`
}

type TagPageParam struct {
	Current int    `json:"current" form:"current"`
	Size    int    `json:"size" form:"size"`
	Keyword string `json:"keyword" form:"keyword"`
}

type TagCreateParam struct {
	Name  string `json:"name" binding:"required"`
	Color string `json:"color"`
}

type TagModifyParam struct {
	ID    string `json:"id" binding:"required"`
	Name  string `json:"name"`
	Color string `json:"color"`
}

type TagRemoveParam pojo.IdsParam
