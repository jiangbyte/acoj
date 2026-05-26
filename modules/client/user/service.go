package user

import (
	"context"
	"time"

	"gorm.io/gorm"

	"hei-gin/core/db"
	"hei-gin/core/enums"
	"hei-gin/core/exception"
	"hei-gin/core/utils"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

func ClientUserPage(c *gin.Context, param *ClientUserPageParam) gin.H {
	ctx := context.Background()
	if param.Current < 1 {
		param.Current = 1
	}
	if param.Size < 1 {
		param.Size = 10
	}
	if param.Size > 100 {
		param.Size = 100
	}

	query := db.DB.WithContext(ctx).Model(&ClientUser{})
	if param.Keyword != "" {
		like := "%" + param.Keyword + "%"
		query = query.Where("username LIKE ? OR nickname LIKE ? OR phone LIKE ? OR email LIKE ?", like, like, like, like)
	}
	if param.Status != "" {
		query = query.Where("status = ?", param.Status)
	}

	var total int64
	query.Count(&total)

	var records []ClientUser
	query.Order("created_at DESC").Limit(param.Size).Offset((param.Current - 1) * param.Size).Find(&records)
	return gin.H{
		"code": 200, "message": "请求成功", "success": true,
		"data": gin.H{
			"records": records,
			"total":   total,
			"current": param.Current,
			"size":    param.Size,
			"pages":   int((total + int64(param.Size) - 1) / int64(param.Size)),
		},
	}
}

func ClientUserDetail(c *gin.Context, id string) *ClientUser {
	if id == "" {
		return nil
	}
	ctx := context.Background()
	var entity ClientUser
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil
		}
		panic(exception.NewBusinessError("查询用户详情失败: "+err.Error(), 500))
	}
	return &entity
}

func ClientUserCreate(c *gin.Context, vo *ClientUserVO, userID string) {
	ctx := context.Background()
	now := time.Now()

	entity := ClientUser{
		ID:        utils.GenerateID(),
		Status: string(enums.UserStatusActive),
		CreatedAt: &now,
		UpdatedAt: &now,
	}
	if vo.Username != nil {
		var count int64
		db.DB.WithContext(ctx).Model(&ClientUser{}).Where("username = ?", *vo.Username).Count(&count)
		if count > 0 {
			panic(exception.NewBusinessError("帐号已存在", 400))
		}
		entity.Username = vo.Username
	}
	if vo.Password != nil {
		hashed, err := bcrypt.GenerateFromPassword([]byte(*vo.Password), bcrypt.DefaultCost)
		if err != nil {
			panic(exception.NewBusinessError("密码加密失败", 500))
		}
		s := string(hashed)
		entity.Password = &s
	}
	if vo.Nickname != nil {
		entity.Nickname = vo.Nickname
	}
	if vo.Avatar != nil {
		entity.Avatar = vo.Avatar
	}
	if vo.Email != nil {
		entity.Email = vo.Email
	}
	if vo.Phone != nil {
		entity.Phone = vo.Phone
	}
	if userID != "" {
		entity.CreatedBy = &userID
		entity.UpdatedBy = &userID
	}

	if err := db.DB.WithContext(ctx).Create(&entity).Error; err != nil {
		panic(exception.NewBusinessError("添加用户失败: "+err.Error(), 500))
	}
}

func ClientUserModify(c *gin.Context, vo *ClientUserVO, userID string) {
	ctx := context.Background()
	var entity ClientUser
	if err := db.DB.WithContext(ctx).First(&entity, "id = ?", vo.ID).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			panic(exception.NewBusinessError("数据不存在", 400))
		}
		panic(exception.NewBusinessError("查询用户失败: "+err.Error(), 500))
	}

	updates := map[string]interface{}{"updated_at": time.Now()}
	if vo.Nickname != nil {
		updates["nickname"] = *vo.Nickname
	}
	if vo.Email != nil {
		updates["email"] = *vo.Email
	}
	if vo.Phone != nil {
		updates["phone"] = *vo.Phone
	}
	if vo.Status != "" {
		updates["status"] = vo.Status
	}
	if userID != "" {
		updates["updated_by"] = userID
	}

	if err := db.DB.WithContext(ctx).Model(&ClientUser{}).Where("id = ?", vo.ID).Updates(updates).Error; err != nil {
		panic(exception.NewBusinessError("编辑用户失败: "+err.Error(), 500))
	}
}

func ClientUserRemove(c *gin.Context, ids []string) {
	if len(ids) == 0 {
		return
	}
	ctx := context.Background()
	db.DB.WithContext(ctx).Where("id IN ?", ids).Delete(&ClientUser{})
}

func Current(c *gin.Context) *ClientUser {
	userID := ""
	// Get userID from auth context - pass empty for now
	if userID == "" {
		return nil
	}
	var entity ClientUser
	if err := db.DB.Where("id = ?", userID).First(&entity).Error; err != nil {
		return nil
	}
	return &entity
}

func UpdateProfile(c *gin.Context, param *UpdateProfileParam) {
	userID := ""
	if userID == "" {
		return
	}
	updates := map[string]interface{}{"updated_at": time.Now()}
	if param.Nickname != nil {
		updates["nickname"] = *param.Nickname
	}
	if param.Email != nil {
		updates["email"] = *param.Email
	}
	if param.Phone != nil {
		updates["phone"] = *param.Phone
	}
	db.DB.Model(&ClientUser{}).Where("id = ?", userID).Updates(updates)
}

func UpdateAvatar(c *gin.Context, param *UpdateAvatarParam) {
	userID := ""
	if userID == "" {
		return
	}
	db.DB.Model(&ClientUser{}).Where("id = ?", userID).Update("avatar", param.Avatar)
}

func UpdatePassword(c *gin.Context, param *UpdatePasswordParam) {
	userID := ""
	if userID == "" {
		return
	}
	hashed, err := bcrypt.GenerateFromPassword([]byte(param.NewPassword), bcrypt.DefaultCost)
	if err != nil {
		panic(exception.NewBusinessError("密码加密失败", 500))
	}
	s := string(hashed)
	db.DB.Model(&ClientUser{}).Where("id = ?", userID).Update("password", &s)
}
