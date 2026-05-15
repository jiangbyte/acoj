package user

import (
	"context"
	"errors"
	"time"

	"hei-gin/core/db"
	"hei-gin/core/utils"
	"hei-gin/ent"
	"hei-gin/ent/clientuser"
)

type PageParam struct {
	Page    int    `form:"page" json:"page"`
	Size    int    `form:"size" json:"size"`
	Keyword string `form:"keyword" json:"keyword"`
	Status  string `form:"status" json:"status"`
}

type ClientUserVO struct {
	ID          string `json:"id"`
	Username    string `json:"username"`
	Nickname    string `json:"nickname"`
	Avatar      string `json:"avatar"`
	Email       string `json:"email"`
	Phone       string `json:"phone"`
	Status      string `json:"status"`
	Gender      string `json:"gender"`
	Birthday    string `json:"birthday"`
	Description string `json:"description"`
	CreatedAt   string `json:"created_at"`
	CreatedBy   string `json:"created_by"`
	UpdatedAt   string `json:"updated_at"`
	UpdatedBy   string `json:"updated_by"`
}

type CreateReq struct {
	Username    string  `json:"username" binding:"required"`
	Password    string  `json:"password" binding:"required"`
	Nickname    string  `json:"nickname"`
	Avatar      string  `json:"avatar"`
	Email       string  `json:"email"`
	Phone       string  `json:"phone"`
	Status      string  `json:"status"`
	Gender      string  `json:"gender"`
	Birthday    *string `json:"birthday"`
	Description string  `json:"description"`
}

type ModifyReq struct {
	ID          string  `json:"id" binding:"required"`
	Username    string  `json:"username"`
	Password    string  `json:"password"`
	Nickname    string  `json:"nickname"`
	Avatar      string  `json:"avatar"`
	Email       string  `json:"email"`
	Phone       string  `json:"phone"`
	Status      string  `json:"status"`
	Gender      string  `json:"gender"`
	Birthday    *string `json:"birthday"`
	Description string  `json:"description"`
}

type RemoveReq struct {
	IDs []string `form:"ids" json:"ids" binding:"required"`
}

type DetailReq struct {
	ID string `form:"id" json:"id" binding:"required"`
}

type UpdateProfileReq struct {
	Nickname    string  `json:"nickname"`
	Avatar      string  `json:"avatar"`
	Email       string  `json:"email"`
	Phone       string  `json:"phone"`
	Gender      string  `json:"gender"`
	Birthday    *string `json:"birthday"`
	Description string  `json:"description"`
}

type UpdateAvatarReq struct {
	Avatar string `json:"avatar" binding:"required"`
}

type UpdatePasswordReq struct {
	OldPassword string `json:"old_password" binding:"required"`
	NewPassword string `json:"new_password" binding:"required"`
}

var ClientUserExportFieldNames = map[string]string{
	"username":    "用户名",
	"nickname":    "昵称",
	"email":       "邮箱",
	"phone":       "手机号",
	"gender":      "性别",
	"status":      "状态",
	"birthday":    "出生日期",
	"description": "描述",
	"created_at":  "创建时间",
}

var ClientUserExportFields = []string{"username", "nickname", "email", "phone", "gender", "status", "birthday", "description", "created_at"}

func toVO(u *ent.ClientUser) ClientUserVO {
	vo := ClientUserVO{
		ID:          u.ID,
		Username:    u.Username,
		Nickname:    u.Nickname,
		Avatar:      u.Avatar,
		Email:       u.Email,
		Phone:       u.Phone,
		Status:      u.Status,
		Gender:      u.Gender,
		Description: u.Description,
		CreatedAt:   u.CreatedAt.Format("2006-01-02 15:04:05"),
		CreatedBy:   u.CreatedBy,
		UpdatedAt:   u.UpdatedAt.Format("2006-01-02 15:04:05"),
		UpdatedBy:   u.UpdatedBy,
	}
	if !u.Birthday.IsZero() {
		vo.Birthday = u.Birthday.Format("2006-01-02")
	}
	return vo
}

func Page(page, size int, keyword, status string) (int, []*ent.ClientUser, error) {
	ctx := context.Background()
	q := db.Client.ClientUser.Query()

	if keyword != "" {
		q = q.Where(
			clientuser.Or(
				clientuser.UsernameContains(keyword),
				clientuser.NicknameContains(keyword),
			),
		)
	}
	if status != "" {
		q = q.Where(clientuser.Status(status))
	}

	total, err := q.Count(ctx)
	if err != nil {
		return 0, nil, err
	}

	if size <= 0 {
		size = 10
	}
	if page <= 0 {
		page = 1
	}

	items, err := q.
		Order(ent.Desc(clientuser.FieldCreatedAt)).
		Limit(size).
		Offset((page - 1) * size).
		All(ctx)
	if err != nil {
		return 0, nil, err
	}

	return total, items, nil
}

func Create(req *CreateReq, loginID string) (*ent.ClientUser, error) {
	ctx := context.Background()
	now := time.Now()

	// Decrypt SM2 and hash password
	decrypted, err := utils.SM2Decrypt(req.Password)
	if err != nil {
		return nil, err
	}
	hashed, err := utils.BcryptHash(decrypted)
	if err != nil {
		return nil, err
	}

	q := db.Client.ClientUser.Create().
		SetID(utils.NextID()).
		SetUsername(req.Username).
		SetPassword(hashed).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)

	if req.Nickname != "" {
		q.SetNickname(req.Nickname)
	}
	if req.Avatar != "" {
		q.SetAvatar(req.Avatar)
	}
	if req.Email != "" {
		q.SetEmail(req.Email)
	}
	if req.Phone != "" {
		q.SetPhone(req.Phone)
	}
	if req.Status != "" {
		q.SetStatus(req.Status)
	}
	if req.Gender != "" {
		q.SetGender(req.Gender)
	}
	if req.Birthday != nil && *req.Birthday != "" {
		b, err := time.Parse("2006-01-02", *req.Birthday)
		if err == nil {
			q.SetBirthday(b)
		}
	}
	if req.Description != "" {
		q.SetDescription(req.Description)
	}

	return q.Save(ctx)
}

func Modify(req *ModifyReq, loginID string) (*ent.ClientUser, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.ClientUser.UpdateOneID(req.ID)

	if req.Username != "" {
		u.SetUsername(req.Username)
	}
	if req.Password != "" {
		decrypted, err := utils.SM2Decrypt(req.Password)
		if err != nil {
			return nil, err
		}
		hashed, err := utils.BcryptHash(decrypted)
		if err != nil {
			return nil, err
		}
		u.SetPassword(hashed)
	}
	if req.Nickname != "" {
		u.SetNickname(req.Nickname)
	}
	if req.Avatar != "" {
		u.SetAvatar(req.Avatar)
	}
	if req.Email != "" {
		u.SetEmail(req.Email)
	}
	if req.Phone != "" {
		u.SetPhone(req.Phone)
	}
	if req.Status != "" {
		u.SetStatus(req.Status)
	}
	if req.Gender != "" {
		u.SetGender(req.Gender)
	}
	if req.Birthday != nil && *req.Birthday != "" {
		b, err := time.Parse("2006-01-02", *req.Birthday)
		if err == nil {
			u.SetBirthday(b)
		}
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func Remove(ids []string) error {
	ctx := context.Background()
	_, err := db.Client.ClientUser.Delete().Where(clientuser.IDIn(ids...)).Exec(ctx)
	return err
}

func Detail(id string) (*ent.ClientUser, error) {
	ctx := context.Background()
	return db.Client.ClientUser.Get(ctx, id)
}

// Current returns the current logged-in user's info as a VO.
func Current(userID string) (*ClientUserVO, error) {
	ctx := context.Background()
	user, err := db.Client.ClientUser.Get(ctx, userID)
	if err != nil {
		return nil, err
	}
	vo := toVO(user)
	return &vo, nil
}

func QueryAll() ([]*ent.ClientUser, error) {
	ctx := context.Background()
	return db.Client.ClientUser.Query().Order(ent.Desc(clientuser.FieldCreatedAt)).All(ctx)
}

func ImportRow(row map[string]string, loginID string) error {
	ctx := context.Background()
	now := time.Now()

	hashed, err := utils.BcryptHash("123456")
	if err != nil {
		return err
	}

	q := db.Client.ClientUser.Create().
		SetID(utils.NextID()).
		SetUsername(row["用户名"]).
		SetPassword(hashed).
		SetCreatedAt(now).
		SetCreatedBy(loginID).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID)

	if v := row["昵称"]; v != "" {
		q.SetNickname(v)
	}
	if v := row["邮箱"]; v != "" {
		q.SetEmail(v)
	}
	if v := row["手机号"]; v != "" {
		q.SetPhone(v)
	}
	if v := row["性别"]; v != "" {
		q.SetGender(v)
	}
	if v := row["状态"]; v != "" {
		q.SetStatus(v)
	}

	_, err = q.Save(ctx)
	return err
}

func UpdateProfile(loginID string, req *UpdateProfileReq) (*ent.ClientUser, error) {
	ctx := context.Background()
	now := time.Now()
	u := db.Client.ClientUser.UpdateOneID(loginID)

	if req.Nickname != "" {
		u.SetNickname(req.Nickname)
	}
	if req.Avatar != "" {
		u.SetAvatar(req.Avatar)
	}
	if req.Email != "" {
		u.SetEmail(req.Email)
	}
	if req.Phone != "" {
		u.SetPhone(req.Phone)
	}
	if req.Gender != "" {
		u.SetGender(req.Gender)
	}
	if req.Birthday != nil && *req.Birthday != "" {
		b, err := time.Parse("2006-01-02", *req.Birthday)
		if err == nil {
			u.SetBirthday(b)
		}
	}
	if req.Description != "" {
		u.SetDescription(req.Description)
	}

	return u.SetUpdatedAt(now).SetUpdatedBy(loginID).Save(ctx)
}

func UpdateAvatar(loginID, avatar string) (*ent.ClientUser, error) {
	ctx := context.Background()
	now := time.Now()

	return db.Client.ClientUser.UpdateOneID(loginID).
		SetAvatar(avatar).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID).
		Save(ctx)
}

func UpdatePassword(loginID string, req *UpdatePasswordReq) error {
	ctx := context.Background()

	// Get current user
	user, err := db.Client.ClientUser.Get(ctx, loginID)
	if err != nil {
		return errors.New("用户不存在")
	}

	// Decrypt old password
	decryptedOld, err := utils.SM2Decrypt(req.OldPassword)
	if err != nil {
		return err
	}

	// Verify old password
	if !utils.BcryptVerify(decryptedOld, user.Password) {
		return errors.New("原密码错误")
	}

	// Decrypt new password
	decryptedNew, err := utils.SM2Decrypt(req.NewPassword)
	if err != nil {
		return err
	}

	// Hash new password
	hashed, err := utils.BcryptHash(decryptedNew)
	if err != nil {
		return err
	}

	now := time.Now()
	_, err = db.Client.ClientUser.UpdateOneID(loginID).
		SetPassword(hashed).
		SetUpdatedAt(now).
		SetUpdatedBy(loginID).
		Save(ctx)
	return err
}
