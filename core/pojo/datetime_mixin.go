package pojo

import "time"

var datetimeFormats = []string{
	"2006-01-02 15:04:05",
	"2006-01-02T15:04:05",
	"2006-01-02",
}

// ParseDateTime attempts to parse a string as a datetime using multiple formats.
// Supported formats: "2006-01-02 15:04:05", "2006-01-02T15:04:05", "2006-01-02".
func ParseDateTime(v string) (time.Time, error) {
	for _, fmt := range datetimeFormats {
		if t, err := time.Parse(fmt, v); err == nil {
			return t, nil
		}
	}
	return time.Time{}, &time.ParseError{Layout: datetimeFormats[0], Value: v}
}

// FormatDateTime formats a time.Time as "2006-01-02 15:04:05".
func FormatDateTime(t time.Time) string {
	return t.Format("2006-01-02 15:04:05")
}

// FormatDate formats a time.Time as "2006-01-02".
func FormatDate(t time.Time) string {
	return t.Format("2006-01-02")
}
