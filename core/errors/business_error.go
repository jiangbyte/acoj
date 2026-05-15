package errors

type BusinessError struct {
	Message string
	Code    int
}

func (e *BusinessError) Error() string {
	return e.Message
}

func NewBusinessError(message string, code ...int) *BusinessError {
	c := 400
	if len(code) > 0 {
		c = code[0]
	}
	return &BusinessError{Message: message, Code: c}
}
