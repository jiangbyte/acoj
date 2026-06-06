package sandbox

import (
	"hei-gin/sdk/db"

	"context"
	"log"
	"time"
)

func init() {
	db.RegisterModel(&JudgeSandbox{})
	db.RegisterSeed("default go-judge sandbox", seedDefaultSandbox)
}

func seedDefaultSandbox() error {
	var count int64
	db.DB.Model(&JudgeSandbox{}).Count(&count)
	if count > 0 {
		return nil
	}
	now := time.Now()
	sb := JudgeSandbox{
		ID:        "default-sandbox",
		Name:      "go-judge",
		Endpoint:  "localhost:5051",
		Timeout:   30,
		Status:    "active",
		CreatedAt: &now,
		UpdatedAt: &now,
	}
	if err := db.DB.WithContext(context.TODO()).Create(&sb).Error; err != nil {
		return err
	}
	log.Println("[Seed] Default go-judge sandbox created")
	return nil
}
