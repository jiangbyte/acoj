package pojo

import "time"

// LoginClientUserInfo represents the client/consumer user login information.
type LoginClientUserInfo struct {
	ID              string     `json:"id"`
	Username        string     `json:"username"`
	Password        string     `json:"-"` // excluded from JSON serialization
	Nickname        string     `json:"nickname"`
	Avatar          string     `json:"avatar"`
	Signature       string     `json:"signature"`
	Motto           string     `json:"motto"`
	Gender          string     `json:"gender"`
	Birthday        *time.Time `json:"birthday"`
	Constellation   string     `json:"constellation"`
	RealName        string     `json:"real_name"`
	IDCard          string     `json:"id_card"`
	NativePlace     string     `json:"native_place"`
	Province        string     `json:"province"`
	City            string     `json:"city"`
	District        string     `json:"district"`
	Address         string     `json:"address"`
	ZipCode         string     `json:"zip_code"`
	Phone           string     `json:"phone"`
	Mobile          string     `json:"mobile"`
	Email           string     `json:"email"`
	Wechat          string     `json:"wechat"`
	QQ              string     `json:"qq"`
	Education       string     `json:"education"`
	School          string     `json:"school"`
	Major           string     `json:"major"`
	Website         string     `json:"website"`
	Blog            string     `json:"blog"`
	Github          string     `json:"github"`
	Interests       string     `json:"interests"`
	Bio             string     `json:"bio"`
	MaritalStatus   string     `json:"marital_status"`
	BloodType       string     `json:"blood_type"`
	Height          *float64   `json:"height"`
	Weight          *float64   `json:"weight"`
	HealthStatus    string     `json:"health_status"`
	PoliticalStatus string     `json:"political_status"`
	Nationality     string     `json:"nationality"`
	Ethnicity       string     `json:"ethnicity"`
	Religion        string     `json:"religion"`
	Language        string     `json:"language"`
	Status          string     `json:"status"`
	LastLoginAt     *time.Time `json:"last_login_at"`
	LastLoginIP     string     `json:"last_login_ip"`
	LoginCount      *int       `json:"login_count"`
	Remark          string     `json:"remark"`
}
