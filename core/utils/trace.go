package utils

import (
	"encoding/hex"

	"github.com/google/uuid"
)

// GenerateTraceID generates a new trace ID (UUID without dashes).
func GenerateTraceID() string {
	id := uuid.New()
	return hex.EncodeToString(id[:])
}

// GetTraceID returns the current trace ID from context.
// Placeholder: returns empty string for now.
func GetTraceID() string {
	return ""
}

// SetTraceID sets the current trace ID in context.
// Placeholder: no-op for now.
func SetTraceID(id string) {
	_ = id
}
