package enums

type StatusEnum string

const (
	StatusYes      StatusEnum = "YES"
	StatusNo       StatusEnum = "NO"
	StatusEnabled  StatusEnum = "ENABLED"
	StatusDisabled StatusEnum = "DISABLED"
)

func (e StatusEnum) Desc() string {
	descriptions := map[StatusEnum]string{
		StatusYes:      "是",
		StatusNo:       "否",
		StatusEnabled:  "启用",
		StatusDisabled: "禁用",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}

type UserStatusEnum string

const (
	UserStatusActive   UserStatusEnum = "ACTIVE"
	UserStatusInactive UserStatusEnum = "INACTIVE"
	UserStatusLocked   UserStatusEnum = "LOCKED"
)

func (e UserStatusEnum) Desc() string {
	descriptions := map[UserStatusEnum]string{
		UserStatusActive:   "正常",
		UserStatusInactive: "停用",
		UserStatusLocked:   "锁定",
	}
	if d, ok := descriptions[e]; ok {
		return d
	}
	return ""
}
