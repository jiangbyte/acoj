package db

import (
	"fmt"
	"log"
)

// ===== Model Registration =====

var registeredModels []any

// RegisterModel registers a GORM model for auto-migration.
// Call this from the model package's init() to self-register.
func RegisterModel(model any) {
	registeredModels = append(registeredModels, model)
}

// GetModels returns all registered models for auto-migration.
func GetModels() []any {
	return registeredModels
}

// ===== Seed Registration =====

// Seed describes a seed data function that runs after migration.
type Seed struct {
	Name string
	Run  func() error
}

var seeds []Seed

// RegisterSeed registers a seed data function.
// Seeds run in registration order after auto-migration.
// Each seed should be idempotent (check existence before inserting).
func RegisterSeed(name string, fn func() error) {
	seeds = append(seeds, Seed{Name: name, Run: fn})
}

// RunSeeds executes all registered seed functions.
func RunSeeds() error {
	for _, s := range seeds {
		log.Printf("[Seed] Running: %s", s.Name)
		if err := s.Run(); err != nil {
			return fmt.Errorf("seed %q failed: %w", s.Name, err)
		}
	}
	return nil
}
