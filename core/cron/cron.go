package cron

import (
	"context"
	"log"
	"time"

	contest "hei-gin/modules/judge/contest"
)

// Start begins all background cron jobs.
func Start() {
	go contestStatusJob()
	log.Println("[Cron] Started background jobs")
}

// contestStatusJob checks contest status transitions every minute.
func contestStatusJob() {
	time.Sleep(10 * time.Second)
	contest.StatusTransition(context.Background())

	ticker := time.NewTicker(1 * time.Minute)
	defer ticker.Stop()

	for range ticker.C {
		contest.StatusTransition(context.Background())
	}
}
