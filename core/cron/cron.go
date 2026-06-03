package cron

import (
	"log"
)

// Job is a function that runs on a schedule.
type Job interface {
	Name() string
	Run()
}

var jobs []Job

// Register adds a cron job.
func Register(j Job) {
	jobs = append(jobs, j)
}

// Start begins all background cron jobs.
func Start() {
	for _, j := range jobs {
		go func(job Job) {
			defer func() {
				if r := recover(); r != nil {
					log.Printf("[Cron] Job %s panicked: %v", job.Name(), r)
				}
			}()
			log.Printf("[Cron] Starting job: %s", job.Name())
			job.Run()
		}(j)
	}
	if len(jobs) > 0 {
		log.Printf("[Cron] Started %d background jobs", len(jobs))
	} else {
		log.Println("[Cron] No background jobs registered")
	}
}
