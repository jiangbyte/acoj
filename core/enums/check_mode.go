package enums

// CheckMode represents permission/role checking mode: AND or OR.
type CheckMode string

const (
	CheckModeAND CheckMode = "AND"
	CheckModeOR  CheckMode = "OR"
)

func (e CheckMode) Desc() string {
	switch e {
	case CheckModeAND:
		return "且"
	case CheckModeOR:
		return "或"
	default:
		return ""
	}
}
