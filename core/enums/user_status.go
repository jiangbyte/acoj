package enums

// UserStatus represents the status of a user account.
type UserStatus string

const (
	UserStatusActive   UserStatus = "ACTIVE"
	UserStatusInactive UserStatus = "INACTIVE"
	UserStatusLocked   UserStatus = "LOCKED"
)

func (e UserStatus) Desc() string {
	switch e {
	case UserStatusActive:
		return "正常"
	case UserStatusInactive:
		return "停用"
	case UserStatusLocked:
		return "锁定"
	default:
		return ""
	}
}
