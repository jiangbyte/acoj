package enums

// LoginType represents login type: BUSINESS (backend) or CONSUMER (frontend).
type LoginType string

const (
	LoginTypeBusiness LoginType = "BUSINESS"
	LoginTypeConsumer LoginType = "CONSUMER"
)

func (e LoginType) Desc() string {
	switch e {
	case LoginTypeBusiness:
		return "后台登录"
	case LoginTypeConsumer:
		return "客户端登录"
	default:
		return ""
	}
}
