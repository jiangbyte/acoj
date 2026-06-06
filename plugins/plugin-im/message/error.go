package message

import "fmt"

// AppError represents an application-level error with a message and code.
type AppError struct {
	Message string
	Code    int
}

func (e *AppError) Error() string {
	return fmt.Sprintf("AppError{code=%d, message=%s}", e.Code, e.Message)
}

// NewAppError creates a new AppError.
func NewAppError(message string, code int) *AppError {
	return &AppError{Message: message, Code: code}
}
