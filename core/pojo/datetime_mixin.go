package pojo

import "time"

// DateTimeFormat is the standard datetime format used for serialization.
const DateTimeFormat = "2006-01-02 15:04:05"

// DateTime wraps time.Time with custom JSON formatting matching fastapi's DateTimeValidatorMixin.
type DateTime struct {
	time.Time
}

// NewDateTime creates a DateTime from a time.Time.
func NewDateTime(t time.Time) DateTime {
	return DateTime{Time: t}
}

// MarshalJSON implements json.Marshaler, outputting the time in DateTimeFormat.
func (t DateTime) MarshalJSON() ([]byte, error) {
	if t.IsZero() {
		return []byte("null"), nil
	}
	return []byte(`"` + t.Format(DateTimeFormat) + `"`), nil
}

// UnmarshalJSON implements json.Unmarshaler, accepting multiple datetime formats.
func (t *DateTime) UnmarshalJSON(data []byte) error {
	if string(data) == "null" {
		t.Time = time.Time{}
		return nil
	}
	s := string(data)
	// Strip quotes
	if len(s) >= 2 && s[0] == '"' && s[len(s)-1] == '"' {
		s = s[1 : len(s)-1]
	}
	formats := []string{
		DateTimeFormat,
		"2006-01-02T15:04:05",
		"2006-01-02",
		time.RFC3339,
	}
	for _, f := range formats {
		if parsed, err := time.Parse(f, s); err == nil {
			t.Time = parsed
			return nil
		}
	}
	return t.Time.UnmarshalJSON(data)
}

// String returns the formatted datetime string.
func (t DateTime) String() string {
	if t.IsZero() {
		return ""
	}
	return t.Format(DateTimeFormat)
}
