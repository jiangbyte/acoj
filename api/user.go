package api

type UserAPI interface {
	GetUserNameByID(id string) string
}
