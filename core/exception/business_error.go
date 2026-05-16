package exception

// BusinessError represents a business-level error with a custom code.
// It maps to hei-fastapi's BusinessException but follows Go error conventions.
// Use panic/recover with this type for control flow similar to exception raising.
type BusinessError struct {
	Message string
	Code    int
}

func (e *BusinessError) Error() string {
	return e.Message
}

// NewBusinessError creates a new BusinessError.
// code defaults to 400 if not provided.
func NewBusinessError(message string, code ...int) *BusinessError {
	c := 400
	if len(code) > 0 {
		c = code[0]
	}
	return &BusinessError{Message: message, Code: c}
}
