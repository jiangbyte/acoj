package enums

// Status represents common status values.
type Status string

const (
	StatusYes      Status = "YES"
	StatusNo       Status = "NO"
	StatusEnabled  Status = "ENABLED"
	StatusDisabled Status = "DISABLED"
)

func (e Status) Desc() string {
	switch e {
	case StatusYes:
		return "是"
	case StatusNo:
		return "否"
	case StatusEnabled:
		return "启用"
	case StatusDisabled:
		return "禁用"
	default:
		return ""
	}
}
