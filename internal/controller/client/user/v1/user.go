package v1

import (
	"context"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/util/gconv"

	api "hei-goframe/api/client/user/v1"
	"hei-goframe/internal/service/auth"
	userService "hei-goframe/internal/service/client/user"
)

type ControllerV1 struct{}

func NewV1() *ControllerV1 {
	return &ControllerV1{}
}

func (c *ControllerV1) Page(ctx context.Context, req *api.ClientUserPageReq) (res *api.ClientUserPageRes, err error) {
	result, err := userService.Page(ctx, req.Keyword, req.Status, req.Current, req.Size)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserPageRes{PageRes: *result}, nil
}

func (c *ControllerV1) Create(ctx context.Context, req *api.ClientUserCreateReq) (res *api.ClientUserCreateRes, err error) {
	err = userService.Create(ctx, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone, req.Status)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserCreateRes{}, nil
}

func (c *ControllerV1) Modify(ctx context.Context, req *api.ClientUserModifyReq) (res *api.ClientUserModifyRes, err error) {
	err = userService.Modify(ctx, req.Id, req.Account, req.Nickname, req.Avatar, req.Motto,
		req.Gender, req.Birthday, req.Email, req.Github, req.Phone, req.Status)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserModifyRes{}, nil
}

func (c *ControllerV1) Remove(ctx context.Context, req *api.ClientUserRemoveReq) (res *api.ClientUserRemoveRes, err error) {
	err = userService.Remove(ctx, req.Ids)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserRemoveRes{}, nil
}

func (c *ControllerV1) Detail(ctx context.Context, req *api.ClientUserDetailReq) (res *api.ClientUserDetailRes, err error) {
	data, err := userService.Detail(ctx, req.Id)
	if err != nil {
		return nil, err
	}
	if data == nil {
		return nil, nil
	}
	res = &api.ClientUserDetailRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) Current(ctx context.Context, req *api.ClientUserCurrentReq) (res *api.ClientUserCurrentRes, err error) {
	loginId := getLoginId(ctx)
	data, err := userService.GetCurrentUser(ctx, loginId)
	if err != nil || data == nil {
		return nil, err
	}
	res = &api.ClientUserCurrentRes{}
	if err := gconv.Struct(data, res); err != nil {
		return nil, err
	}
	return res, nil
}

func (c *ControllerV1) UpdateProfile(ctx context.Context, req *api.ClientUserUpdateProfileReq) (res *api.ClientUserUpdateProfileRes, err error) {
	loginId := getLoginId(ctx)
	err = userService.UpdateProfile(ctx, loginId, req.Account, req.Nickname,
		req.Motto, req.Gender, req.Birthday, req.Email, req.Github)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserUpdateProfileRes{}, nil
}

func (c *ControllerV1) UpdateAvatar(ctx context.Context, req *api.ClientUserUpdateAvatarReq) (res *api.ClientUserUpdateAvatarRes, err error) {
	loginId := getLoginId(ctx)
	err = userService.UpdateAvatar(ctx, loginId, req.Avatar)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserUpdateAvatarRes{}, nil
}

func (c *ControllerV1) UpdatePassword(ctx context.Context, req *api.ClientUserUpdatePasswordReq) (res *api.ClientUserUpdatePasswordRes, err error) {
	loginId := getLoginId(ctx)
	err = userService.UpdatePassword(ctx, loginId, req.CurrentPassword, req.NewPassword)
	if err != nil {
		return nil, err
	}
	return &api.ClientUserUpdatePasswordRes{}, nil
}

func getLoginId(ctx context.Context) string {
	if v := ctx.Value(auth.ContextKeyLoginId); v != nil {
		return v.(string)
	}
	r := g.RequestFromCtx(ctx)
	if r == nil {
		return ""
	}
	tokenStr := r.Header.Get(auth.ConsumerAuth.GetTokenName())
	id, _ := auth.ConsumerAuth.GetLoginId(ctx, tokenStr)
	return id
}
