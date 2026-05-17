package clientuser

import (
	"context"
	"time"

	"hei-gin/core/auth"
	"hei-gin/core/db"
	"hei-gin/core/exception"
	"hei-gin/core/result"
	"hei-gin/core/utils"
	gen "hei-gin/ent/gen"
	"hei-gin/ent/gen/clientuser"

	"entgo.io/ent/dialect/sql"
	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

// ---------------------------------------------------------------------------
// Time helpers
// ---------------------------------------------------------------------------

func formatDate(t *time.Time) string {
	if t == nil {
		return ""
	}
	return t.Format("2006-01-02")
}

func formatTime(t *time.Time) string {
	if t == nil {
		return ""
	}
	return t.Format("2006-01-02 15:04:05")
}

func parseDate(s string) *time.Time {
	if s == "" {
		return nil
	}
	t, err := time.Parse("2006-01-02", s)
	if err != nil {
		return nil
	}
	return &t
}

// ---------------------------------------------------------------------------
// entToVO
// ---------------------------------------------------------------------------

func entToVO(entity *gen.ClientUser) ClientUserVO {
	if entity == nil {
		return ClientUserVO{}
	}

	vo := ClientUserVO{
		ID:         entity.ID,
		Status:     entity.Status,
		LoginCount: entity.LoginCount,
	}

	if entity.Username != nil {
		vo.Username = *entity.Username
	}
	if entity.Nickname != nil {
		vo.Nickname = *entity.Nickname
	}
	if entity.Avatar != nil {
		vo.Avatar = *entity.Avatar
	}
	if entity.Motto != nil {
		vo.Motto = *entity.Motto
	}
	if entity.Gender != nil {
		vo.Gender = *entity.Gender
	}
	vo.Birthday = formatDate(entity.Birthday)
	if entity.Email != nil {
		vo.Email = *entity.Email
	}
	if entity.Github != nil {
		vo.Github = *entity.Github
	}
	vo.LastLoginAt = formatTime(entity.LastLoginAt)
	if entity.LastLoginIP != nil {
		vo.LastLoginIP = *entity.LastLoginIP
	}
	vo.CreatedAt = formatTime(entity.CreatedAt)
	if entity.CreatedBy != nil {
		vo.CreatedBy = *entity.CreatedBy
	}
	vo.UpdatedAt = formatTime(entity.UpdatedAt)
	if entity.UpdatedBy != nil {
		vo.UpdatedBy = *entity.UpdatedBy
	}

	return vo
}

// ---------------------------------------------------------------------------
// Page
// ---------------------------------------------------------------------------

func Page(c *gin.Context, param *ClientUserPageParam) gin.H {
	ctx := context.Background()

	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}

	query := db.Client.ClientUser.Query()
	if param.Keyword != "" {
		query = query.Where(clientuser.Or(
			clientuser.UsernameContains(param.Keyword),
			clientuser.NicknameContains(param.Keyword),
		))
	}
	if param.Status != "" {
		query = query.Where(clientuser.StatusEQ(param.Status))
	}

	total, err := query.Count(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户列表失败: "+err.Error(), 500))
	}

	offset := (param.Current - 1) * param.Size
	records, err := query.Clone().
		Order(clientuser.ByCreatedAt(sql.OrderDesc())).
		Limit(param.Size).
		Offset(offset).
		All(ctx)
	if err != nil {
		panic(exception.NewBusinessError("查询用户列表失败: "+err.Error(), 500))
	}

	vos := make([]ClientUserVO, len(records))
	for i, r := range records {
		vos[i] = entToVO(r)
	}

	return result.PageDataResult(c, vos, total, param.Current, param.Size)
}

// ---------------------------------------------------------------------------
// Create
// ---------------------------------------------------------------------------

func Create(c *gin.Context, param *ClientUserCreateParam) {
	ctx := context.Background()

	// Check username uniqueness
	exists, err := db.Client.ClientUser.Query().
		Where(clientuser.UsernameEQ(param.Username)).
		Exist(ctx)
	if err == nil && exists {
		panic(exception.NewBusinessError("账号已存在", 400))
	}

	// SM2 decrypt password
	decryptedPassword := utils.Decrypt(param.Password)
	if decryptedPassword == "" {
		panic(exception.NewBusinessError("密码解密失败", 400))
	}

	// bcrypt hash
	hashed, err := bcrypt.GenerateFromPassword([]byte(decryptedPassword), bcrypt.DefaultCost)
	if err != nil {
		panic(exception.NewBusinessError("密码加密失败", 500))
	}

	now := time.Now()
	userID := auth.GetLoginIDDefaultNull(c)

	builder := db.Client.ClientUser.Create().
		SetID(utils.GenerateID()).
		SetUsername(param.Username).
		SetPassword(string(hashed)).
		SetCreatedAt(now).
		SetUpdatedAt(now)

	if param.Nickname != nil {
		builder.SetNillableNickname(param.Nickname)
	}
	if param.Email != nil {
		builder.SetNillableEmail(param.Email)
	}
	if param.Avatar != nil {
		builder.SetNillableAvatar(param.Avatar)
	}
	if param.Motto != nil {
		builder.SetNillableMotto(param.Motto)
	}
	if param.Gender != nil {
		builder.SetNillableGender(param.Gender)
	}
	if param.Birthday != "" {
		builder.SetNillableBirthday(parseDate(param.Birthday))
	}
	if param.Github != nil {
		builder.SetNillableGithub(param.Github)
	}
	if param.Status != "" {
		builder.SetStatus(param.Status)
	}
	if userID != "" {
		builder.SetCreatedBy(userID).SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("添加用户失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// Modify
// ---------------------------------------------------------------------------

func Modify(c *gin.Context, param *ClientUserModifyParam) {
	ctx := context.Background()

	if param.ID == "" {
		panic(exception.NewBusinessError("ID不能为空", 400))
	}

	// Verify exists
	entity, err := db.Client.ClientUser.Get(ctx, param.ID)
	if err != nil {
		if gen.IsNotFound(err) {
			panic(exception.NewBusinessError("数据不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	// If username changed, check uniqueness
	if param.Username != "" {
		currentUsername := ""
		if entity.Username != nil {
			currentUsername = *entity.Username
		}
		if param.Username != currentUsername {
			exists, err := db.Client.ClientUser.Query().
				Where(clientuser.UsernameEQ(param.Username)).
				Exist(ctx)
			if err == nil && exists {
				panic(exception.NewBusinessError("账号已存在", 400))
			}
		}
	}

	now := time.Now()
	userID := auth.GetLoginIDDefaultNull(c)

	builder := db.Client.ClientUser.UpdateOneID(param.ID).
		SetUpdatedAt(now)

	if param.Username != "" {
		builder.SetUsername(param.Username)
	}
	if param.Nickname != nil {
		builder.SetNillableNickname(param.Nickname)
	}
	if param.Email != nil {
		builder.SetNillableEmail(param.Email)
	}
	if param.Avatar != nil {
		builder.SetNillableAvatar(param.Avatar)
	}
	if param.Motto != nil {
		builder.SetNillableMotto(param.Motto)
	}
	if param.Gender != nil {
		builder.SetNillableGender(param.Gender)
	}
	if param.Birthday != "" {
		builder.SetNillableBirthday(parseDate(param.Birthday))
	}
	if param.Github != nil {
		builder.SetNillableGithub(param.Github)
	}
	if param.Status != "" {
		builder.SetStatus(param.Status)
	}
	if userID != "" {
		builder.SetUpdatedBy(userID)
	}

	_, err = builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("编辑用户失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// Remove
// ---------------------------------------------------------------------------

func Remove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()

	_, err := db.Client.ClientUser.Delete().
		Where(clientuser.IDIn(ids...)).
		Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("删除用户失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// Detail
// ---------------------------------------------------------------------------

func Detail(c *gin.Context, id string) *ClientUserVO {
	if id == "" {
		return nil
	}
	ctx := context.Background()

	entity, err := db.Client.ClientUser.Get(ctx, id)
	if err != nil {
		if gen.IsNotFound(err) {
			return nil
		}
		panic(exception.NewBusinessError("查询用户详情失败: "+err.Error(), 500))
	}

	vo := entToVO(entity)
	return &vo
}

// ---------------------------------------------------------------------------
// Current
// ---------------------------------------------------------------------------

func Current(c *gin.Context) *ClientUserVO {
	userID := auth.NewHeiClientAuthTool().GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	entity, err := db.Client.ClientUser.Get(ctx, userID)
	if err != nil {
		if gen.IsNotFound(err) {
			panic(exception.NewBusinessError("用户不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	vo := entToVO(entity)
	return &vo
}

// ---------------------------------------------------------------------------
// UpdateProfile
// ---------------------------------------------------------------------------

func UpdateProfile(c *gin.Context, param *UpdateProfileParam) {
	userID := auth.NewHeiClientAuthTool().GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	now := time.Now()
	builder := db.Client.ClientUser.UpdateOneID(userID).SetUpdatedAt(now)

	if param.Nickname != nil {
		builder.SetNillableNickname(param.Nickname)
	}
	if param.Motto != nil {
		builder.SetNillableMotto(param.Motto)
	}
	if param.Gender != nil {
		builder.SetNillableGender(param.Gender)
	}
	if param.Birthday != "" {
		builder.SetNillableBirthday(parseDate(param.Birthday))
	}
	if param.Email != nil {
		builder.SetNillableEmail(param.Email)
	}
	if param.Github != nil {
		builder.SetNillableGithub(param.Github)
	}

	_, err := builder.Save(ctx)
	if err != nil {
		panic(exception.NewBusinessError("更新个人信息失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// UpdateAvatar
// ---------------------------------------------------------------------------

func UpdateAvatar(c *gin.Context, param *UpdateAvatarParam) {
	userID := auth.NewHeiClientAuthTool().GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	err := db.Client.ClientUser.UpdateOneID(userID).
		SetAvatar(param.Avatar).
		Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("更新头像失败: "+err.Error(), 500))
	}
}

// ---------------------------------------------------------------------------
// UpdatePassword
// ---------------------------------------------------------------------------

func UpdatePassword(c *gin.Context, param *UpdatePasswordParam) {
	userID := auth.NewHeiClientAuthTool().GetLoginIDDefaultNull(c)
	if userID == "" {
		panic(exception.NewBusinessError("用户未登录", 401))
	}
	ctx := context.Background()

	entity, err := db.Client.ClientUser.Get(ctx, userID)
	if err != nil {
		if gen.IsNotFound(err) {
			panic(exception.NewBusinessError("用户不存在", 404))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	if entity.Password == nil || *entity.Password == "" {
		panic(exception.NewBusinessError("未设置密码，无法修改", 400))
	}

	currentPassword := utils.Decrypt(param.CurrentPassword)
	err = bcrypt.CompareHashAndPassword([]byte(*entity.Password), []byte(currentPassword))
	if err != nil {
		panic(exception.NewBusinessError("当前密码不正确", 400))
	}

	newPassword := utils.Decrypt(param.NewPassword)
	hashed, err := bcrypt.GenerateFromPassword([]byte(newPassword), bcrypt.DefaultCost)
	if err != nil {
		panic(exception.NewBusinessError("密码加密失败", 500))
	}

	err = db.Client.ClientUser.UpdateOneID(userID).
		SetPassword(string(hashed)).
		Exec(ctx)
	if err != nil {
		panic(exception.NewBusinessError("修改密码失败: "+err.Error(), 500))
	}
}
